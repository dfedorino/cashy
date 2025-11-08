package com.dfedorino.cashy.service;

import com.dfedorino.cashy.domain.model.account.AccountBalanceEntity;
import com.dfedorino.cashy.domain.model.category.CategoryBalanceEntity;
import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.model.operation.OperationEntity;
import com.dfedorino.cashy.domain.repository.account.AccountBalanceRepository;
import com.dfedorino.cashy.domain.repository.category.CategoryBalanceRepository;
import com.dfedorino.cashy.domain.repository.category.CategoryRepository;
import com.dfedorino.cashy.domain.repository.operation.OperationRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.service.dto.OperationDto;
import com.dfedorino.cashy.service.exception.category.CategoryNotFoundException;
import com.dfedorino.cashy.service.exception.user.UserNotFoundException;
import com.dfedorino.cashy.service.exception.user.UserNotLoggedInException;
import com.dfedorino.cashy.util.AuthorisationUtil;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BinaryOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class OperationService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final OperationRepository operationRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final CategoryBalanceRepository categoryBalanceRepository;

    private final TransactionTemplate tx;

    public OperationDto createIncomeOperation(BigDecimal amount, String categoryName) {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }

        String userLogin = AuthorisationUtil.getCurrentUser().login();

        return safeTx($ -> {
            Long userId = getUserId(userLogin);

            CategoryEntity category = findCategory(categoryName, userId);

            OperationEntity operation = operationRepository.createOperation(
                    new OperationEntity(userId, category.getId(), amount));

            AccountBalanceEntity updatedAccountBalance =
                    updateAccountBalance(userId,
                                         operation.getAmount(),
                                         BigDecimal::add);

            CategoryBalanceEntity updatedCategoryBalance =
                    updateIncomeCategoryBalance(userId,
                                                category.getId(),
                                                operation.getAmount());

            return new OperationDto(userLogin,
                                    category.getName(),
                                    operation.getAmount(),
                                    updatedCategoryBalance.getCurrentBalance(),
                                    null,
                                    updatedAccountBalance.getBalance()
            );
        });
    }

    public OperationDto createExpenseOperation(BigDecimal amount, String categoryName) {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }

        String userLogin = AuthorisationUtil.getCurrentUser().login();

        return safeTx($ -> {
            Long userId = getUserId(userLogin);

            var category = findCategory(categoryName, userId);

            var operation = operationRepository.createOperation(
                    new OperationEntity(userId, category.getId(), amount));

            var updatedAccountBalance = updateAccountBalance(userId,
                                                             operation.getAmount(),
                                                             BigDecimal::subtract);

            var updatedCategoryBalance = updateExpenseCategoryBalance(userId,
                                                                      category.getId(),
                                                                      operation.getAmount());

            return new OperationDto(
                    userLogin,
                    category.getName(),
                    operation.getAmount(),
                    updatedCategoryBalance.getCurrentBalance(),
                    updatedCategoryBalance.getRemainingBalance(),
                    updatedAccountBalance.getBalance()
            );
        });
    }

    private Long getUserId(String userLogin) {
        return userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException(userLogin))
                .getId();
    }

    private CategoryEntity findCategory(String categoryName, Long userId) {
        return categoryRepository.findByUserIdAndName(userId, categoryName)
                .orElseThrow(() -> new CategoryNotFoundException(categoryName));
    }

    private AccountBalanceEntity updateAccountBalance(Long userId,
                                                      BigDecimal operationAmount,
                                                      BinaryOperator<BigDecimal> accountBalanceChange
    ) {
        var accountBalance = accountBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User without account balance"));

        BigDecimal newAccountBalance = accountBalanceChange.apply(accountBalance.getBalance(),
                                                                  operationAmount);

        return accountBalanceRepository.updateBalanceByUserId(userId, newAccountBalance)
                .orElseThrow();
    }

    private CategoryBalanceEntity updateIncomeCategoryBalance(Long userId,
                                                              Long categoryId,
                                                              BigDecimal operationAmount
    ) {
        var categoryBalance = categoryBalanceRepository.findByUserIdAndCategoryId(userId,
                                                                                  categoryId)
                .orElseThrow(() -> new IllegalStateException("Category without balance!"));

        BigDecimal newCategoryBalance = categoryBalance.getCurrentBalance()
                .add(operationAmount);

        return categoryBalanceRepository.updateCurrentBalanceByUserIdAndCategoryId(
                userId,
                categoryId,
                newCategoryBalance).orElseThrow();
    }

    private CategoryBalanceEntity updateExpenseCategoryBalance(Long userId,
                                                               Long categoryId,
                                                               BigDecimal operationAmount
    ) {
        var categoryBalance = categoryBalanceRepository.findByUserIdAndCategoryId(userId,
                                                                                  categoryId)
                .orElseThrow(() -> new IllegalStateException("Category without balance!"));

        BigDecimal newCategoryBalance = categoryBalance.getCurrentBalance()
                .add(operationAmount);

        if (categoryBalance.getRemainingBalance() != null) {
            BigDecimal newRemainingBalance = categoryBalance.getRemainingBalance()
                    .subtract(operationAmount);

            categoryBalanceRepository
                    .updateRemainingBalanceByUserIdAndCategoryId(userId,
                                                                 categoryId,
                                                                 newRemainingBalance);
        }

        return categoryBalanceRepository.updateCurrentBalanceByUserIdAndCategoryId(
                        userId,
                        categoryId,
                        newCategoryBalance)
                .orElseThrow();
    }

    private <T> T safeTx(TransactionCallback<T> callback) {
        return Objects.requireNonNull(tx.execute(callback));
    }
}
