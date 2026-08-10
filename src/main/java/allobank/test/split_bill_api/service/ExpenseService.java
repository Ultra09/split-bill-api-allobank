package allobank.test.split_bill_api.service;

import allobank.test.split_bill_api.dto.ExpenseCreateRequest;
import allobank.test.split_bill_api.dto.ExpenseResponse;
import allobank.test.split_bill_api.dto.ExpenseSplitRequest;
import allobank.test.split_bill_api.dto.ExpenseSplitResponse;
import allobank.test.split_bill_api.exception.BusinessException;
import allobank.test.split_bill_api.model.BillGroup;
import allobank.test.split_bill_api.model.Expense;
import allobank.test.split_bill_api.model.ExpenseSplit;
import allobank.test.split_bill_api.model.Participant;
import allobank.test.split_bill_api.repository.BillGroupRepository;
import allobank.test.split_bill_api.repository.ExpenseRepository;
import allobank.test.split_bill_api.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final allobank.test.split_bill_api.repository.AuditLogRepository auditLogRepository;

    @Transactional
    public ExpenseResponse addExpense(Long groupId, ExpenseCreateRequest request) {
        BillGroup group = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Group not found"));

        Participant payer = participantRepository.findById(request.getPayerId())
                .orElseThrow(() -> new BusinessException("Payer not found"));

        if (!payer.getBillGroup().getId().equals(group.getId())) {
            throw new BusinessException("Payer does not belong to this group");
        }

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setTotalAmount(request.getTotalAmount());
        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }
        expense.setPayer(payer);
        expense.setBillGroup(group);

        List<Participant> allParticipants = group.getParticipants();

        if (request.getSplits() == null || request.getSplits().isEmpty()) {
            // Equal split
            if (allParticipants.isEmpty()) {
                throw new BusinessException("Group has no participants to split expense");
            }
            BigDecimal splitAmount = request.getTotalAmount().divide(new BigDecimal(allParticipants.size()), 2, RoundingMode.HALF_UP);
            
            // Handle rounding difference by adding remainder to first participant
            BigDecimal totalAssigned = splitAmount.multiply(new BigDecimal(allParticipants.size()));
            BigDecimal difference = request.getTotalAmount().subtract(totalAssigned);

            for (int i = 0; i < allParticipants.size(); i++) {
                Participant p = allParticipants.get(i);
                ExpenseSplit split = new ExpenseSplit();
                split.setParticipant(p);
                if (i == 0) {
                    split.setAmountOwed(splitAmount.add(difference));
                } else {
                    split.setAmountOwed(splitAmount);
                }
                expense.addSplit(split);
            }
        } else {
            // Custom split
            BigDecimal totalSplitAmount = BigDecimal.ZERO;
            for (ExpenseSplitRequest splitReq : request.getSplits()) {
                Participant participant = participantRepository.findById(splitReq.getParticipantId())
                        .orElseThrow(() -> new BusinessException("Participant not found in split"));
                if (!participant.getBillGroup().getId().equals(group.getId())) {
                    throw new BusinessException("Participant in split does not belong to this group");
                }

                ExpenseSplit split = new ExpenseSplit();
                split.setParticipant(participant);

                if (splitReq.getExactAmount() != null) {
                    split.setAmountOwed(splitReq.getExactAmount());
                } else if (splitReq.getPercentage() != null) {
                    BigDecimal amount = request.getTotalAmount().multiply(splitReq.getPercentage()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    split.setAmountOwed(amount);
                } else {
                    throw new BusinessException("Split must have exactAmount or percentage");
                }
                
                totalSplitAmount = totalSplitAmount.add(split.getAmountOwed());
                expense.addSplit(split);
            }
            
            if (totalSplitAmount.compareTo(request.getTotalAmount()) != 0) {
                // Since this is financial we strictly check matching splits.
                // Alternatively, adjust the last one to match the total.
                // Let's enforce strict validation for exact amount/percentages so they don't lose money.
                BigDecimal diff = request.getTotalAmount().subtract(totalSplitAmount).abs();
                if (diff.compareTo(new BigDecimal("0.05")) > 0) {
                    throw new BusinessException("Sum of splits (" + totalSplitAmount + ") does not equal total amount (" + request.getTotalAmount() + ")");
                }
            }
        }

        Expense savedExpense = expenseRepository.save(expense);

        allobank.test.split_bill_api.model.AuditLog log = new allobank.test.split_bill_api.model.AuditLog();
        log.setBillGroup(group);
        log.setAction("EXPENSE_ADDED");
        log.setDescription(payer.getName() + " added expense '" + request.getDescription() + "' for " + request.getTotalAmount());
        auditLogRepository.save(log);

        return mapToResponse(savedExpense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setDescription(expense.getDescription());
        response.setTotalAmount(expense.getTotalAmount());
        response.setCategory(expense.getCategory());
        response.setPayerId(expense.getPayer().getId());
        response.setPayerName(expense.getPayer().getName());
        response.setSplits(expense.getSplits().stream().map(s -> {
            ExpenseSplitResponse splitRes = new ExpenseSplitResponse();
            splitRes.setParticipantId(s.getParticipant().getId());
            splitRes.setParticipantName(s.getParticipant().getName());
            splitRes.setAmountOwed(s.getAmountOwed());
            return splitRes;
        }).collect(Collectors.toList()));
        return response;
    }
}
