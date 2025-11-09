package com.dfedorino.cashy.service.dto;

import java.math.BigDecimal;

public record OperationDto(
        String userLogin,
        String categoryName,
        BigDecimal categoryLimit,
        Integer categoryAlertThreshold,
        BigDecimal operationAmount,
        BigDecimal categoryBalanceAfterOperation,
        BigDecimal categoryRemainingLimitAfterOperation,
        BigDecimal totalBalanceAfterOperation
) {

}
