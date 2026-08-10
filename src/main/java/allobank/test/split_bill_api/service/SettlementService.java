package allobank.test.split_bill_api.service;

import allobank.test.split_bill_api.dto.SettlementResponse;
import allobank.test.split_bill_api.dto.SettlementTransaction;
import allobank.test.split_bill_api.exception.BusinessException;
import allobank.test.split_bill_api.model.BillGroup;
import allobank.test.split_bill_api.model.Expense;
import allobank.test.split_bill_api.model.ExpenseSplit;
import allobank.test.split_bill_api.model.Participant;
import allobank.test.split_bill_api.repository.BillGroupRepository;
import allobank.test.split_bill_api.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final BillGroupRepository billGroupRepository;
    private final ExpenseRepository expenseRepository;
    private final allobank.test.split_bill_api.repository.PaymentRepository paymentRepository;
    private final PersonalizationService personalizationService;

    public SettlementResponse calculateSettlement(Long groupId) {
        BillGroup group = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Group not found"));

        List<Expense> expenses = expenseRepository.findByBillGroupId(groupId);

        // Calculate net balances
        // Positive balance means the person is owed money (Creditor)
        // Negative balance means the person owes money (Debtor)
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        Map<Long, Participant> participantMap = new HashMap<>();
        for (Participant p : group.getParticipants()) {
            netBalances.put(p.getId(), BigDecimal.ZERO);
            participantMap.put(p.getId(), p);
        }

        BigDecimal totalGroupExpenses = BigDecimal.ZERO;
        Map<allobank.test.split_bill_api.model.Category, BigDecimal> categorySummaries = new HashMap<>();

        for (Expense expense : expenses) {
            totalGroupExpenses = totalGroupExpenses.add(expense.getTotalAmount());
            
            allobank.test.split_bill_api.model.Category cat = expense.getCategory();
            categorySummaries.put(cat, categorySummaries.getOrDefault(cat, BigDecimal.ZERO).add(expense.getTotalAmount()));

            // Add to payer's balance (they are owed this money)
            Participant payer = expense.getPayer();
            netBalances.put(payer.getId(), netBalances.get(payer.getId()).add(expense.getTotalAmount()));

            // Subtract from each split participant's balance (they owe this money)
            for (ExpenseSplit split : expense.getSplits()) {
                Participant splitParticipant = split.getParticipant();
                netBalances.put(splitParticipant.getId(), netBalances.get(splitParticipant.getId()).subtract(split.getAmountOwed()));
            }
        }

        // Apply payments to reduce debts
        List<allobank.test.split_bill_api.model.Payment> payments = paymentRepository.findByBillGroupId(groupId);
        for (allobank.test.split_bill_api.model.Payment payment : payments) {
            // Payer loses money (so their credit decreases / debt increases)
            netBalances.put(payment.getPayer().getId(), netBalances.get(payment.getPayer().getId()).subtract(payment.getAmount()));
            // Receiver gains money (so their credit increases / debt decreases)
            netBalances.put(payment.getReceiver().getId(), netBalances.get(payment.getReceiver().getId()).add(payment.getAmount()));
        }

        // Separate debtors and creditors
        PriorityQueue<ParticipantBalance> debtors = new PriorityQueue<>((a, b) -> a.balance.compareTo(b.balance)); // ascending, most negative first
        PriorityQueue<ParticipantBalance> creditors = new PriorityQueue<>((a, b) -> b.balance.compareTo(a.balance)); // descending, most positive first

        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.offer(new ParticipantBalance(participantMap.get(entry.getKey()), entry.getValue()));
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.offer(new ParticipantBalance(participantMap.get(entry.getKey()), entry.getValue()));
            }
        }

        List<SettlementTransaction> transactions = new ArrayList<>();

        // Greedy algorithm for settlement optimization
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            ParticipantBalance debtor = debtors.poll();
            ParticipantBalance creditor = creditors.poll();

            BigDecimal debtAmount = debtor.balance.abs();
            BigDecimal creditAmount = creditor.balance;

            BigDecimal settledAmount = debtAmount.min(creditAmount);

            transactions.add(new SettlementTransaction(debtor.participant.getName(), creditor.participant.getName(), settledAmount));

            debtor.balance = debtor.balance.add(settledAmount);
            creditor.balance = creditor.balance.subtract(settledAmount);

            // Re-insert if they still have balance (using 0.01 tolerance for floating point-like issues, though BigDecimal should be exact)
            if (debtor.balance.compareTo(new BigDecimal("-0.01")) < 0) {
                debtors.offer(debtor);
            }
            if (creditor.balance.compareTo(new BigDecimal("0.01")) > 0) {
                creditors.offer(creditor);
            }
        }

        BigDecimal pct = personalizationService.calculateServiceChargePct();
        BigDecimal amount = personalizationService.calculateServiceChargeAmount(totalGroupExpenses);

        return new SettlementResponse(groupId, transactions, categorySummaries, pct, amount);
    }

    private static class ParticipantBalance {
        Participant participant;
        BigDecimal balance;

        public ParticipantBalance(Participant participant, BigDecimal balance) {
            this.participant = participant;
            this.balance = balance;
        }
    }
}
