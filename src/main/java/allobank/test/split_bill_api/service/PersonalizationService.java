package allobank.test.split_bill_api.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PersonalizationService {

    private static final String GITHUB_USERNAME = "ultra09";

    public BigDecimal calculateServiceChargePct() {
        int asciiSum = 0;
        for (char c : GITHUB_USERNAME.toCharArray()) {
            asciiSum += c;
        }
        int percentage = asciiSum % 10;
        return new BigDecimal(percentage);
    }

    public BigDecimal calculateServiceChargeAmount(BigDecimal totalGroupExpenses) {
        BigDecimal pct = calculateServiceChargePct();
        if (pct.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalGroupExpenses.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
