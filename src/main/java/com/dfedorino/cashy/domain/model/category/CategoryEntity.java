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
    private Long kindId;
    private String name;
    private BigDecimal limitAmount;
    private Integer alertThreshold;
    private LocalDateTime createdAt;
}
