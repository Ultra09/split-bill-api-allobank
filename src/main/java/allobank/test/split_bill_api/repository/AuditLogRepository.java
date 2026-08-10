package allobank.test.split_bill_api.repository;

import allobank.test.split_bill_api.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByBillGroupIdOrderByTimestampDesc(Long billGroupId);
}
