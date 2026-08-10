package allobank.test.split_bill_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal totalAmount;
    private allobank.test.split_bill_api.model.Category category;
    private Long payerId;
    private String payerName;
    private List<ExpenseSplitResponse> splits;
}
