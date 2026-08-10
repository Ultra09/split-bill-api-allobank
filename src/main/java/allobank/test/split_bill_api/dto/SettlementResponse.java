package allobank.test.split_bill_api.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    private Long groupId;
    private List<SettlementTransaction> transactions;
    private java.util.Map<allobank.test.split_bill_api.model.Category, BigDecimal> categorySummaries;
    private BigDecimal serviceChargePct;
    private BigDecimal serviceChargeAmount;
}
