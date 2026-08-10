package allobank.test.split_bill_api.repository;

import allobank.test.split_bill_api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBillGroupId(Long billGroupId);
}
