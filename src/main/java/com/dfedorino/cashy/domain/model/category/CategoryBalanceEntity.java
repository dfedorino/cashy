package com.dfedorino.cashy.domain.model.category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBalanceEntity {

    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal remainingBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
