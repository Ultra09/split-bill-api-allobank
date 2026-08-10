package allobank.test.split_bill_api.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementTransaction {
    private String from; // Who owes
    private String to;   // Who gets paid
    private BigDecimal amount;
}
