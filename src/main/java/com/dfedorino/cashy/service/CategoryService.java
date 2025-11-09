package com.dfedorino.cashy.service;

import com.dfedorino.cashy.domain.model.category.CategoryBalanceEntity;
import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.model.transaction.TransactionTypes;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryBalanceRepository;
import com.dfedorino.cashy.domain.repository.category.CategoryRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.service.dto.CategoryDto;
import com.dfedorino.cashy.service.exception.category.CategoryNotFoundException;
import com.dfedorino.cashy.service.exception.user.UserNotFoundException;
import com.dfedorino.cashy.service.exception.user.UserNotLoggedInException;
import com.dfedorino.cashy.util.AuthorisationUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class CategoryService {

    public static final String DEFAULT_ALERT_THRESHOLD = "default-alert-threshold";
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryBalanceRepository categoryBalanceRepository;
    private final TransactionTemplate tx;
    private final Environment env;

    public CategoryDto createIncomeCategory(String categoryName,
                                            BigDecimal initialBalance) {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }
        return safeTx($ -> {
            String userLogin = AuthorisationUtil.getCurrentUser().login();
            UserEntity user = userRepository.findByLogin(userLogin)
                    .orElseThrow(() -> new UserNotFoundException(userLogin));

            CategoryEntity createdCategory = categoryRepository.createCategory(new CategoryEntity(
                    user.getId(),
                    TransactionTypes.INCOME.getId(),
                    categoryName
            ));

            CategoryBalanceEntity categoryBalance = categoryBalanceRepository.create(
                    new CategoryBalanceEntity(
                            user.getId(),
                            createdCategory.getId(),
                            initialBalance
                    ));

            return new CategoryDto(
                    userLogin,
                    createdCategory.getName(),
                    TransactionTypes.of(createdCategory.getTransactionTypeId()),
                    createdCategory.getLimitAmount(),
                    createdCategory.getAlertThreshold(),
                    categoryBalance.getCurrentBalance(),
                    categoryBalance.getRemainingBalance()
            );

        });
    }

    public CategoryDto createExpenseCategory(String categoryName) {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }
        return safeTx($ -> {
            String userLogin = AuthorisationUtil.getCurrentUser().login();
            UserEntity user = userRepository.findByLogin(userLogin)
                    .orElseThrow(() -> new UserNotFoundException(userLogin));

            CategoryEntity createdCategory = categoryRepository.createCategory(new CategoryEntity(
                    user.getId(),
                    TransactionTypes.EXPENSE.getId(),
                    categoryName
            ));

            CategoryBalanceEntity categoryBalance = categoryBalanceRepository.create(
                    new CategoryBalanceEntity(
                            user.getId(),
                            createdCategory.getId(),
                            BigDecimal.ZERO
                    ));

            return new CategoryDto(
                    userLogin,
                    createdCategory.getName(),
                    TransactionTypes.of(createdCategory.getTransactionTypeId()),
                    createdCategory.getLimitAmount(),
                    createdCategory.getAlertThreshold(),
                    categoryBalance.getCurrentBalance(),
                    categoryBalance.getRemainingBalance()
            );

        });
    }

    public CategoryDto createExpenseCategory(String categoryName,
                                             BigDecimal limit,
                                             Integer alertThreshold) {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }
        return safeTx($ -> {
            String userLogin = AuthorisationUtil.getCurrentUser().login();
            UserEntity user = userRepository.findByLogin(userLogin)
                    .orElseThrow(() -> new UserNotFoundException(userLogin));

            CategoryEntity createdCategory = categoryRepository.createCategory(new CategoryEntity(
                    user.getId(),
                    TransactionTypes.EXPENSE.getId(),
                    categoryName,
                    limit,
                    alertThreshold == null ?
                            env.getProperty(DEFAULT_ALERT_THRESHOLD, Integer.class) :
                            alertThreshold
            ));

            CategoryBalanceEntity categoryBalance = categoryBalanceRepository.create(
                    new CategoryBalanceEntity(
                            user.getId(),
                            createdCategory.getId(),
                            BigDecimal.ZERO,
                            limit
                    ));

            return new CategoryDto(
                    userLogin,
                    createdCategory.getName(),
                    TransactionTypes.of(createdCategory.getTransactionTypeId()),
                    createdCategory.getLimitAmount(),
                    createdCategory.getAlertThreshold(),
                    categoryBalance.getCurrentBalance(),
                    categoryBalance.getRemainingBalance()
            );

        });
    }

    public CategoryDto editCategoryName(String categoryName, String newCategoryName) {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }
        return safeTx($ -> {
            String userLogin = AuthorisationUtil.getCurrentUser().login();
            UserEntity user = userRepository.findByLogin(userLogin)
                    .orElseThrow(() -> new UserNotFoundException(userLogin));
            return categoryRepository.updateNameByUserIdAndName(user.getId(),
                                                                categoryName,
                                                                newCategoryName)
                    .map(category -> getCategoryBalanceAndBuildDto(category, user))
                    .orElseThrow(() -> new CategoryNotFoundException(categoryName));
        });
    }

    public CategoryDto editCategoryLimit(String categoryName, BigDecimal newLimit) {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }
        return safeTx($ -> {
            String userLogin = AuthorisationUtil.getCurrentUser().login();
            UserEntity user = userRepository.findByLogin(userLogin)
                    .orElseThrow(() -> new UserNotFoundException(userLogin));
            return categoryRepository.updateLimitAmountByUserIdAndName(user.getId(),
                                                                       categoryName,
                                                                       newLimit)
                    .map(category -> getCategoryBalanceAndBuildDto(category, user))
                    .orElseThrow(() -> new CategoryNotFoundException(categoryName));
        });
    }

    public Optional<CategoryDto> findByName(String categoryName) {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }

        return safeTx($ -> {
            String userLogin = AuthorisationUtil.getCurrentUser().login();
            UserEntity user = userRepository.findByLogin(userLogin)
                    .orElseThrow(() -> new UserNotFoundException(userLogin));

            return categoryRepository.findByUserIdAndName(
                    user.getId(),
                    categoryName
            ).map(foundCategory -> {
                CategoryBalanceEntity categoryBalance =
                        categoryBalanceRepository.findByUserIdAndCategoryId(user.getId(),
                                                                            foundCategory.getId())
                                .orElseThrow(() -> new IllegalStateException(
                                        "Category without balance!"));

                return new CategoryDto(
                        userLogin,
                        foundCategory.getName(),
                        TransactionTypes.of(foundCategory.getTransactionTypeId()),
                        foundCategory.getLimitAmount(),
                        foundCategory.getAlertThreshold(),
                        categoryBalance.getCurrentBalance(),
                        categoryBalance.getRemainingBalance()
                );
            });
        });
    }


    public List<CategoryDto> findAllCategories() {
        if (!AuthorisationUtil.isUserLoggedIn()) {
            throw new UserNotLoggedInException();
        }
        String userLogin = AuthorisationUtil.getCurrentUser().login();
        UserEntity user = userRepository.findByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException(userLogin));

        return safeTx($ -> categoryRepository.findByUserId(user.getId()).stream()
                .map(category -> getCategoryBalanceAndBuildDto(category, user))
                .toList());
    }

    private CategoryDto getCategoryBalanceAndBuildDto(CategoryEntity category, UserEntity user) {
        var categoryBalance =
                categoryBalanceRepository.findByUserIdAndCategoryId(user.getId(),
                                                                    category.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Category without balance"));
        return new CategoryDto(
                user.getLogin(),
                category.getName(),
                TransactionTypes.of(category.getTransactionTypeId()),
                category.getLimitAmount(),
                category.getAlertThreshold(),
                categoryBalance.getCurrentBalance(),
                categoryBalance.getRemainingBalance()
        );
    }

    private <T> T safeTx(TransactionCallback<T> callback) {
        return Objects.requireNonNull(tx.execute(callback));
    }
}
