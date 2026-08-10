package allobank.test.split_bill_api.controller;

import allobank.test.split_bill_api.dto.ExpenseCreateRequest;
import allobank.test.split_bill_api.dto.ExpenseResponse;
import allobank.test.split_bill_api.dto.GroupCreateRequest;
import allobank.test.split_bill_api.dto.GroupResponse;
import allobank.test.split_bill_api.dto.SettlementResponse;
import allobank.test.split_bill_api.service.ExpenseService;
import allobank.test.split_bill_api.service.GroupService;
import allobank.test.split_bill_api.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;
    private final allobank.test.split_bill_api.service.PaymentService paymentService;
    private final allobank.test.split_bill_api.repository.AuditLogRepository auditLogRepository;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupCreateRequest request) {
        return new ResponseEntity<>(groupService.createGroup(request), HttpStatus.CREATED);
    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<ExpenseResponse> addExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody ExpenseCreateRequest request) {
        return new ResponseEntity<>(expenseService.addExpense(groupId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/settlement")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable Long id) {
        SettlementResponse response = settlementService.calculateSettlement(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Void> recordPayment(@PathVariable Long id, @Valid @RequestBody allobank.test.split_bill_api.dto.PaymentCreateRequest request) {
        paymentService.recordPayment(id, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<java.util.List<allobank.test.split_bill_api.dto.AuditLogResponse>> getAuditLogs(@PathVariable Long id) {
        java.util.List<allobank.test.split_bill_api.model.AuditLog> logs = auditLogRepository.findByBillGroupIdOrderByTimestampDesc(id);
        java.util.List<allobank.test.split_bill_api.dto.AuditLogResponse> responses = logs.stream().map(log -> {
            allobank.test.split_bill_api.dto.AuditLogResponse response = new allobank.test.split_bill_api.dto.AuditLogResponse();
            response.setId(log.getId());
            response.setAction(log.getAction());
            response.setDescription(log.getDescription());
            response.setTimestamp(log.getTimestamp());
            return response;
        }).toList();
        return ResponseEntity.ok(responses);
    }
}
