package com.dfedorino.cashy.domain.model.operation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationEntity {

    private Long id;
    private Long userId;
    private Long kindId;
    private Long categoryId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
