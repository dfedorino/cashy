package com.dfedorino.cashy.ui.cli.command.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.NonNull;

public class CategoryBudgetUtils {

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    public static boolean isThresholdReached(@NonNull BigDecimal limit,
                                             @NonNull BigDecimal remainingLimit,
                                             int alertThreshold) {

        if (limit.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Limit is zero");
        }

        return limit.subtract(remainingLimit)
                .divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .compareTo(new BigDecimal(alertThreshold)) >= 0;
    }
}
