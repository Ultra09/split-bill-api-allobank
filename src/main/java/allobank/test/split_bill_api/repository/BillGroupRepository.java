package allobank.test.split_bill_api.repository;

import allobank.test.split_bill_api.model.BillGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillGroupRepository extends JpaRepository<BillGroup, Long> {
}
