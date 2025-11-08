package com.dfedorino.cashy.domain.model.category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    private Long id;
    private Long userId;
    private Long transactionTypeId;
    private String name;
    private BigDecimal limitAmount;
    private Integer alertThreshold;
    private LocalDateTime createdAt;

    public CategoryEntity(Long userId,
                          Long transactionTypeId,
                          String name
    ) {
        this(userId, transactionTypeId, name, null, null);
    }

    public CategoryEntity(Long userId,
                          Long transactionTypeId,
                          String name,
                          BigDecimal limitAmount,
                          Integer alertThreshold
    ) {
        this(null, userId, transactionTypeId, name, limitAmount, alertThreshold, null);
    }
}
