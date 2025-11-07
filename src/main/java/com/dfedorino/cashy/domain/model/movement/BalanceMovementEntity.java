package com.dfedorino.cashy.domain.model.movement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceMovementEntity {

    private Long id;
    private Long operationId;
    private Long accountBalanceId;
    private Long categoryBalanceId;
    private Long directionTypeId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
