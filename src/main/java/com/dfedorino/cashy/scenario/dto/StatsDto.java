package com.dfedorino.cashy.scenario.dto;

import com.dfedorino.cashy.service.dto.ShortCategoryDto;
import java.math.BigDecimal;
import java.util.List;

public record StatsDto(
        BigDecimal totalIncomeAmount,
        List<ShortCategoryDto> incomeCategories,
        BigDecimal totalExpenseAmount,
        List<ShortCategoryDto> expenseCategories
) {

}
