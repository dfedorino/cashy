package com.dfedorino.cashy.scenario;

import com.dfedorino.cashy.domain.model.transaction.TransactionTypes;
import com.dfedorino.cashy.scenario.dto.ScenarioResult;
import com.dfedorino.cashy.scenario.dto.StatsDto;
import com.dfedorino.cashy.service.CategoryService;
import com.dfedorino.cashy.service.OperationService;
import com.dfedorino.cashy.service.UserService;
import com.dfedorino.cashy.service.dto.CategoryDto;
import com.dfedorino.cashy.service.dto.OperationDto;
import com.dfedorino.cashy.service.dto.ShortCategoryDto;
import com.dfedorino.cashy.service.dto.UserDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final UserService userService;
    private final CategoryService categoryService;
    private final OperationService operationService;

    public ScenarioResult<UserDto> login(String login, String password) {
        return executeScenario(() -> userService.findByLogin(login).isPresent() ?
                userService.loginUser(login, password) :
                userService.registerUser(login, password));
    }

    public ScenarioResult<OperationDto> topUp(String categoryName, BigDecimal amount) {
        return executeScenario(() -> {
            categoryService.findByName(categoryName)
                    .orElseGet(() -> categoryService.createIncomeCategory(categoryName,
                                                                          BigDecimal.ZERO));
            return operationService.createIncomeOperation(amount, categoryName);
        });
    }

    public ScenarioResult<CategoryDto> budget(String categoryName,
                                              BigDecimal limit,
                                              Integer alertThreshold) {
        return executeScenario(() -> categoryService.createExpenseCategory(categoryName,
                                                                           limit,
                                                                           alertThreshold));
    }

    public ScenarioResult<OperationDto> withdraw(String categoryName, BigDecimal amount) {
        return executeScenario(() -> {
            Optional<CategoryDto> category = categoryService.findByName(categoryName);

            if (category.isEmpty()) {
                categoryService.createExpenseCategory(categoryName);
            }

            return operationService.createExpenseOperation(amount, categoryName);
        });
    }

    public ScenarioResult<StatsDto> stats() {
        return executeScenario(() -> {
            List<CategoryDto> categories = categoryService.findAllCategories();

            BigDecimal totalIncomeAmount = BigDecimal.ZERO;
            List<ShortCategoryDto> incomeCategories = new ArrayList<>();
            BigDecimal totalExpenseAmount = BigDecimal.ZERO;
            List<ShortCategoryDto> expenseCategories = new ArrayList<>();

            for (CategoryDto category : categories) {
                ShortCategoryDto shortCategory = new ShortCategoryDto(
                        category.categoryName(),
                        category.currentBalance(),
                        category.limit(),
                        category.remainingBalance()
                );
                if (category.transactionType() == TransactionTypes.INCOME) {
                    totalIncomeAmount = totalIncomeAmount.add(category.currentBalance());
                    incomeCategories.add(shortCategory);
                } else {
                    totalExpenseAmount = totalExpenseAmount.add(category.currentBalance());
                    expenseCategories.add(shortCategory);
                }
            }

            return new StatsDto(totalIncomeAmount,
                                incomeCategories,
                                totalExpenseAmount,
                                expenseCategories);
        });
    }

    private <T> ScenarioResult<T> executeScenario(Callable<T> scenario) {
        try {
            return ScenarioResult.success(scenario.call());
        } catch (Exception e) {
            return ScenarioResult.failure(e);
        }
    }

}
