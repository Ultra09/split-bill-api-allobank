package allobank.test.split_bill_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentCreateRequest {

    @NotNull(message = "Payer ID is required")
    private Long payerId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
}
