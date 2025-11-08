package com.dfedorino.cashy.jdbc.repository.category;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.domain.model.category.CategoryBalanceHistoryEntity;
import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.model.transaction.TransactionTypes;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryBalanceHistoryRepository;
import com.dfedorino.cashy.domain.repository.category.CategoryRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.math.BigDecimal;
import java.util.Set;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcCategoryBalanceHistoryRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private CategoryBalanceHistoryRepository categoryBalanceHistoryRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        categoryRepository = ctx.getBean(CategoryRepository.class);
        categoryBalanceHistoryRepository = ctx.getBean(CategoryBalanceHistoryRepository.class);
    }

    @Test
    void create_income_category_balance_history_entry() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var category = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.INCOME.getId(),
                TestConstants.INCOME_CATEGORY
        )));

        var categoryBalanceHistoryEntry = tx(
                () -> categoryBalanceHistoryRepository.createHistoryEntry(
                        new CategoryBalanceHistoryEntity(
                                user.getId(),
                                category.getId(),
                                BigDecimal.ZERO
                        )));

        assertThat(categoryBalanceHistoryEntry.getId()).isOne();
        assertThat(categoryBalanceHistoryEntry.getUserId()).isEqualTo(user.getId());
        assertThat(categoryBalanceHistoryEntry.getCategoryId()).isEqualTo(category.getId());
        assertThat(categoryBalanceHistoryEntry.getCurrentBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(categoryBalanceHistoryEntry.getRemainingBalance()).isNull();
        assertThat(categoryBalanceHistoryEntry.getCreatedAt()).isNotNull();
    }

    @Test
    void create_expense_category_balance_history_entry() {
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

        var categoryBalanceHistoryEntry = tx(
                () -> categoryBalanceHistoryRepository.createHistoryEntry(
                        new CategoryBalanceHistoryEntity(
                                user.getId(),
                                category.getId(),
                                BigDecimal.ZERO,
                                TestConstants.LIMIT_AMOUNT
                        )));

        assertThat(categoryBalanceHistoryEntry.getId()).isOne();
        assertThat(categoryBalanceHistoryEntry.getUserId()).isEqualTo(user.getId());
        assertThat(categoryBalanceHistoryEntry.getCategoryId()).isEqualTo(category.getId());
        assertThat(categoryBalanceHistoryEntry.getCurrentBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(categoryBalanceHistoryEntry.getRemainingBalance())
                .isEqualTo(TestConstants.LIMIT_AMOUNT);
        assertThat(categoryBalanceHistoryEntry.getCreatedAt()).isNotNull();
    }

    @Test
    void find_entries_for_several_categories() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var incomeCategory = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.INCOME.getId(),
                TestConstants.INCOME_CATEGORY
        )));

        var incomeCategoryBalanceHistoryEntry = tx(
                () -> categoryBalanceHistoryRepository.createHistoryEntry(
                        new CategoryBalanceHistoryEntity(
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

        var expenseCategoryBalanceHistoryEntry = tx(
                () -> categoryBalanceHistoryRepository.createHistoryEntry(
                        new CategoryBalanceHistoryEntity(
                                user.getId(),
                                expenseCategory.getId(),
                                BigDecimal.ZERO,
                                TestConstants.LIMIT_AMOUNT
                        )));

        var historyEntries = tx(
                () -> categoryBalanceHistoryRepository.findByUserIdAndCategoryIds(
                        user.getId(),
                        Set.of(incomeCategory.getId(), expenseCategory.getId(), Long.MAX_VALUE)
                ));

        assertThat(historyEntries)
                .usingRecursiveFieldByFieldElementComparator(
                        RecursiveComparisonConfiguration.builder()
                                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                                .build())
                .containsExactlyInAnyOrder(incomeCategoryBalanceHistoryEntry,
                                           expenseCategoryBalanceHistoryEntry);
    }
}