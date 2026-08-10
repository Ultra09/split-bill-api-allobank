package allobank.test.split_bill_api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExpenseSplitResponse {
    private Long participantId;
    private String participantName;
    private BigDecimal amountOwed;
}
