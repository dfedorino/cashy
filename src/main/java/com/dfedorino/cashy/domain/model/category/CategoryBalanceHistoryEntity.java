package com.dfedorino.cashy.domain.model.category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBalanceHistoryEntity {

    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal currentBalance;
    private BigDecimal remainingBalance;
    private LocalDateTime createdAt;

    public CategoryBalanceHistoryEntity(Long userId,
                                        Long categoryId,
                                        BigDecimal currentBalance) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.currentBalance = currentBalance;
    }

    public CategoryBalanceHistoryEntity(Long userId,
                                        Long categoryId,
                                        BigDecimal currentBalance,
                                        BigDecimal remainingBalance) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.currentBalance = currentBalance;
        this.remainingBalance = remainingBalance;
    }
}