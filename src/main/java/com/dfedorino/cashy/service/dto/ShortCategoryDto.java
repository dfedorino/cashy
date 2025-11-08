package com.dfedorino.cashy.service.dto;

import java.math.BigDecimal;

public record ShortCategoryDto(
        String categoryName,
        BigDecimal currentBalance,
        BigDecimal limit,
        BigDecimal remainingBalance
) {
}
