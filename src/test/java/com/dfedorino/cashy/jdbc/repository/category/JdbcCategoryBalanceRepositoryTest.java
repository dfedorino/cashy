package com.dfedorino.cashy.jdbc.repository.category;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.domain.model.category.CategoryBalanceEntity;
import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.model.transaction.TransactionTypes;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryBalanceRepository;
import com.dfedorino.cashy.domain.repository.category.CategoryRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcCategoryBalanceRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private CategoryBalanceRepository categoryBalanceRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        categoryRepository = ctx.getBean(CategoryRepository.class);
        categoryBalanceRepository = ctx.getBean(CategoryBalanceRepository.class);
    }

    @Test
    void create_income_category_balance() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var category = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.INCOME.getId(),
                TestConstants.INCOME_CATEGORY
        )));

        var categoryBalance = tx(() -> categoryBalanceRepository.create(new CategoryBalanceEntity(
                user.getId(),
                category.getId(),
                BigDecimal.ZERO
        )));

        assertThat(categoryBalance.getId()).isOne();
        assertThat(categoryBalance.getUserId()).isEqualTo(user.getId());
        assertThat(categoryBalance.getCategoryId()).isEqualTo(category.getId());
        assertThat(categoryBalance.getCurrentBalance()).isZero();
        assertThat(categoryBalance.getRemainingBalance()).isNull();
        assertThat(categoryBalance.getCreatedAt()).isNotNull();
        assertThat(categoryBalance.getUpdatedAt()).isNotNull();
        assertThat(categoryBalance.getCreatedAt()).isEqualTo(categoryBalance.getUpdatedAt());
    }

    @Test
    void create_expense_category_balance() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var category = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.EXPENSE.getId(),
                TestConstants.EXPENSE_CATEGORY,
                TestConstants.LIMIT_AMOUNT,
                TestConstants.ALERT_THRESHOLD
        )));

        var categoryBalance = tx(() -> categoryBalanceRepository.create(new CategoryBalanceEntity(
                user.getId(),
                category.getId(),
                BigDecimal.ZERO,
                TestConstants.LIMIT_AMOUNT
        )));

        assertThat(categoryBalance.getId()).isOne();
        assertThat(categoryBalance.getUserId()).isEqualTo(user.getId());
        assertThat(categoryBalance.getCategoryId()).isEqualTo(category.getId());
        assertThat(categoryBalance.getCurrentBalance()).isZero();
        assertThat(categoryBalance.getRemainingBalance()).isEqualTo(TestConstants.LIMIT_AMOUNT);
        assertThat(categoryBalance.getCreatedAt()).isNotNull();
        assertThat(categoryBalance.getUpdatedAt()).isNotNull();
        assertThat(categoryBalance.getCreatedAt()).isEqualTo(categoryBalance.getUpdatedAt());
    }

    @Test
    void update_current_balance() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var category = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.INCOME.getId(),
                TestConstants.INCOME_CATEGORY
        )));

        var categoryBalance = tx(() -> categoryBalanceRepository.create(new CategoryBalanceEntity(
                user.getId(),
                category.getId(),
                BigDecimal.ZERO
        )));

        var updatedCategoryBalance = tx(
                () -> categoryBalanceRepository.updateCurrentBalanceByUserIdAndCategoryId(
                        user.getId(),
                        category.getId(),
                        BigDecimal.TEN
                ));

        assertThat(updatedCategoryBalance).isNotEmpty();

        assertThat(updatedCategoryBalance.get())
                .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                                                  .withIgnoredFields("currentBalance", "updatedAt")
                                                  .build())
                .isEqualTo(categoryBalance);

        assertThat(updatedCategoryBalance.get().getCurrentBalance())
                .isEqualByComparingTo(BigDecimal.TEN);
        assertThat(updatedCategoryBalance.get().getUpdatedAt())
                .isNotEqualTo(updatedCategoryBalance.get().getCreatedAt());
    }

    @Test
    void update_remaining_balance() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var category = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.EXPENSE.getId(),
                TestConstants.EXPENSE_CATEGORY,
                TestConstants.LIMIT_AMOUNT,
                TestConstants.ALERT_THRESHOLD
        )));

        var categoryBalance = tx(() -> categoryBalanceRepository.create(new CategoryBalanceEntity(
                user.getId(),
                category.getId(),
                BigDecimal.ZERO,
                TestConstants.LIMIT_AMOUNT
        )));

        var updatedCategoryBalance = tx(
                () -> categoryBalanceRepository.updateRemainingBalanceByUserIdAndCategoryId(
                        user.getId(),
                        category.getId(),
                        BigDecimal.TEN
                ));

        assertThat(updatedCategoryBalance).isNotEmpty();

        assertThat(updatedCategoryBalance.get())
                .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                                                  .withIgnoredFields("remainingBalance",
                                                                     "updatedAt")
                                                  .withComparatorForType(BigDecimal::compareTo,
                                                                         BigDecimal.class)
                                                  .build())
                .isEqualTo(categoryBalance);

        assertThat(updatedCategoryBalance.get().getRemainingBalance())
                .isEqualByComparingTo(BigDecimal.TEN);
        assertThat(updatedCategoryBalance.get().getUpdatedAt())
                .isNotEqualTo(updatedCategoryBalance.get().getCreatedAt());
    }

    @Test
    void find_category_balances() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var incomeCategory = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.INCOME.getId(),
                TestConstants.INCOME_CATEGORY
        )));

        var incomeCategoryBalance = tx(() -> categoryBalanceRepository.create(
                new CategoryBalanceEntity(
                        user.getId(),
                        incomeCategory.getId(),
                        BigDecimal.ZERO
                )));

        var expenseCategory = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.EXPENSE.getId(),
                TestConstants.EXPENSE_CATEGORY,
                TestConstants.LIMIT_AMOUNT,
                TestConstants.ALERT_THRESHOLD
        )));

        var expenseCategoryBalance = tx(() -> categoryBalanceRepository.create(
                new CategoryBalanceEntity(
                        user.getId(),
                        expenseCategory.getId(),
                        BigDecimal.ZERO,
                        TestConstants.LIMIT_AMOUNT
                )));

        List<CategoryBalanceEntity> foundCategoryBalances = tx(
                () -> categoryBalanceRepository.findByUserId(user.getId())
        );

        assertThat(foundCategoryBalances)
                .usingRecursiveFieldByFieldElementComparator(
                        RecursiveComparisonConfiguration.builder()
                                .withComparatorForType(BigDecimal::compareTo,
                                                       BigDecimal.class)
                                .build())
                .containsExactlyInAnyOrder(incomeCategoryBalance, expenseCategoryBalance);
    }
}