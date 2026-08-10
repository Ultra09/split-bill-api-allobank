package allobank.test.split_bill_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseCreateRequest {
    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal totalAmount;

    private allobank.test.split_bill_api.model.Category category;

    @NotNull(message = "Payer ID is required")
    private Long payerId;

    // Optional list of specific splits. If empty, it means split equally among all participants
    private List<ExpenseSplitRequest> splits;
}
