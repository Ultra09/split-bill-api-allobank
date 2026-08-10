package allobank.test.split_bill_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseSplitRequest {
    @NotNull(message = "Participant ID is required")
    private Long participantId;

    private BigDecimal exactAmount;
    private BigDecimal percentage;
}
