package allobank.test.split_bill_api.service;

import allobank.test.split_bill_api.dto.PaymentCreateRequest;
import allobank.test.split_bill_api.exception.BusinessException;
import allobank.test.split_bill_api.model.BillGroup;
import allobank.test.split_bill_api.model.Participant;
import allobank.test.split_bill_api.model.Payment;
import allobank.test.split_bill_api.repository.BillGroupRepository;
import allobank.test.split_bill_api.repository.ParticipantRepository;
import allobank.test.split_bill_api.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final allobank.test.split_bill_api.repository.AuditLogRepository auditLogRepository;

    @Transactional
    public void recordPayment(Long groupId, PaymentCreateRequest request) {
        if (request.getPayerId().equals(request.getReceiverId())) {
            throw new BusinessException("Payer and receiver cannot be the same person");
        }

        BillGroup group = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Group not found"));

        Participant payer = participantRepository.findById(request.getPayerId())
                .orElseThrow(() -> new BusinessException("Payer not found"));
        
        Participant receiver = participantRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new BusinessException("Receiver not found"));

        if (!payer.getBillGroup().getId().equals(groupId) || !receiver.getBillGroup().getId().equals(groupId)) {
            throw new BusinessException("Both participants must belong to the specified group");
        }

        Payment payment = new Payment();
        payment.setBillGroup(group);
        payment.setPayer(payer);
        payment.setReceiver(receiver);
        payment.setAmount(request.getAmount());

        paymentRepository.save(payment);

        allobank.test.split_bill_api.model.AuditLog log = new allobank.test.split_bill_api.model.AuditLog();
        log.setBillGroup(group);
        log.setAction("PAYMENT_RECORDED");
        log.setDescription(payer.getName() + " paid " + receiver.getName() + " " + request.getAmount());
        auditLogRepository.save(log);
    }
}
