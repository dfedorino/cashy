package com.dfedorino.cashy.service.dto;

import com.dfedorino.cashy.domain.model.transaction.TransactionTypes;
import java.math.BigDecimal;

public record CategoryDto(
        String userLogin,
        String categoryName,
        TransactionTypes transactionType,
        BigDecimal limit,
        Integer alertThreshold,
        BigDecimal currentBalance,
        BigDecimal remainingBalance
) {
}
