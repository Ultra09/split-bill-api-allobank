package allobank.test.split_bill_api.service;

import allobank.test.split_bill_api.dto.SettlementResponse;
import allobank.test.split_bill_api.dto.SettlementTransaction;
import allobank.test.split_bill_api.model.BillGroup;
import allobank.test.split_bill_api.model.Expense;
import allobank.test.split_bill_api.model.ExpenseSplit;
import allobank.test.split_bill_api.model.Participant;
import allobank.test.split_bill_api.repository.BillGroupRepository;
import allobank.test.split_bill_api.repository.ExpenseRepository;
import allobank.test.split_bill_api.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class SettlementServiceTest {

    @Mock
    private BillGroupRepository billGroupRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PersonalizationService personalizationService;

    @InjectMocks
    private SettlementService settlementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateSettlement() {
        // Arrange
        BillGroup group = new BillGroup();
        group.setId(1L);
        group.setName("Test Group");

        Participant p1 = new Participant(1L, "Alice", group);
        Participant p2 = new Participant(2L, "Bob", group);
        Participant p3 = new Participant(3L, "Charlie", group);

        group.setParticipants(List.of(p1, p2, p3));

        // Expense 1: Alice paid 90, split equally (30 each)
        Expense e1 = new Expense();
        e1.setTotalAmount(new BigDecimal("90.00"));
        e1.setPayer(p1);
        e1.setSplits(List.of(
                new ExpenseSplit(1L, e1, p1, new BigDecimal("30.00")),
                new ExpenseSplit(2L, e1, p2, new BigDecimal("30.00")),
                new ExpenseSplit(3L, e1, p3, new BigDecimal("30.00"))
        ));

        // Expense 2: Bob paid 60, split equally (20 each)
        Expense e2 = new Expense();
        e2.setTotalAmount(new BigDecimal("60.00"));
        e2.setPayer(p2);
        e2.setSplits(List.of(
                new ExpenseSplit(4L, e2, p1, new BigDecimal("20.00")),
                new ExpenseSplit(5L, e2, p2, new BigDecimal("20.00")),
                new ExpenseSplit(6L, e2, p3, new BigDecimal("20.00"))
        ));

        // Net balances:
        // Alice: Paid 90, owes 50 (30+20). Net: +40 (Creditor)
        // Bob: Paid 60, owes 50 (30+20). Net: +10 (Creditor)
        // Charlie: Paid 0, owes 50 (30+20). Net: -50 (Debtor)
        
        // Settlement expectation: Charlie pays Alice 40, Charlie pays Bob 10.

        when(billGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(expenseRepository.findByBillGroupId(1L)).thenReturn(List.of(e1, e2));
        when(paymentRepository.findByBillGroupId(1L)).thenReturn(List.of());
        when(personalizationService.calculateServiceChargePct()).thenReturn(new BigDecimal("9.00")); // assume ultra09 gives 9
        when(personalizationService.calculateServiceChargeAmount(new BigDecimal("150.00"))).thenReturn(new BigDecimal("13.50"));

        // Act
        SettlementResponse response = settlementService.calculateSettlement(1L);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("9.00"), response.getServiceChargePct());
        assertEquals(new BigDecimal("13.50"), response.getServiceChargeAmount());
        assertEquals(2, response.getTransactions().size());

        // Check transactions
        boolean charlieToAliceFound = false;
        boolean charlieToBobFound = false;

        for (SettlementTransaction t : response.getTransactions()) {
            if (t.getFrom().equals("Charlie") && t.getTo().equals("Alice")) {
                assertEquals(0, new BigDecimal("40.00").compareTo(t.getAmount()));
                charlieToAliceFound = true;
            } else if (t.getFrom().equals("Charlie") && t.getTo().equals("Bob")) {
                assertEquals(0, new BigDecimal("10.00").compareTo(t.getAmount()));
                charlieToBobFound = true;
            }
        }

        assert(charlieToAliceFound);
        assert(charlieToBobFound);
    }
}
