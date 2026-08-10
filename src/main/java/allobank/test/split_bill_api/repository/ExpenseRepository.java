package allobank.test.split_bill_api.repository;

import allobank.test.split_bill_api.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBillGroupId(Long billGroupId);
}
