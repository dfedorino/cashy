package com.dfedorino.cashy.jdbc.repository.category;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.domain.model.category.CategoryEntity;
import com.dfedorino.cashy.domain.model.transaction.TransactionTypes;
import com.dfedorino.cashy.domain.model.user.UserEntity;
import com.dfedorino.cashy.domain.repository.category.CategoryRepository;
import com.dfedorino.cashy.domain.repository.user.UserRepository;
import com.dfedorino.cashy.jdbc.repository.AbstractJdbcRepositoryTestSkeleton;
import java.math.BigDecimal;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcCategoryRepositoryTest extends AbstractJdbcRepositoryTestSkeleton {

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        userRepository = ctx.getBean(UserRepository.class);
        categoryRepository = ctx.getBean(CategoryRepository.class);
    }

    @Test
    void create_income_category() {
        var user = tx(() -> userRepository.createUser(new UserEntity(
                TestConstants.LOGIN, TestConstants.PASSWORD_HASH
        )));

        var category = tx(() -> categoryRepository.createCategory(new CategoryEntity(
                user.getId(),
                TransactionTypes.INCOME.getId(),
                TestConstants.INCOME_CATEGORY
        )));

        assertThat(category.getId()).isOne();
        assertThat(category.getUserId()).isEqualTo(user.getId());
        assertThat(category.getTransactionTypeId()).isEqualTo(TransactionTypes.INCOME.getId());
        assertThat(category.getName()).isEqualTo(TestConstants.INCOME_CATEGORY);
        assertThat(category.getLimitAmount()).isNull();
        assertThat(category.getAlertThreshold()).isNull();
        assertThat(category.getCreatedAt()).isNotNull();
    }

    @Test
    void create_expense_category() {
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

        assertThat(category.getId()).isOne();
        assertThat(category.getUserId()).isEqualTo(user.getId());
        assertThat(category.getTransactionTypeId()).isEqualTo(TransactionTypes.EXPENSE.getId());
        assertThat(category.getName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(category.getLimitAmount()).isEqualTo(TestConstants.LIMIT_AMOUNT);
        assertThat(category.getAlertThreshold()).isEqualTo(TestConstants.ALERT_THRESHOLD);
        assertThat(category.getCreatedAt()).isNotNull();

        assertThat(categoryRepository.findByUserId(user.getId()))
                .contains(category);
    }

    @Test
    void update_name() {
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

        var updatedCategory = tx(
                () -> categoryRepository.updateNameByUserIdAndName(user.getId(),
                                                                   category.getName(),
                                                                   "shopping"));

        assertThat(updatedCategory).isNotEmpty();

        assertThat(updatedCategory.get())
                .usingRecursiveComparison(
                        RecursiveComparisonConfiguration.builder()
                                .withIgnoredFields("name")
                                .build())
                .isEqualTo(category);

        assertThat(updatedCategory.get().getName()).isEqualTo("shopping");

        assertThat(categoryRepository.findByUserId(user.getId()))
                .contains(updatedCategory.get());

    }

    @Test
    void update_limit_amount() {
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

        var updatedCategory = tx(
                () -> categoryRepository.updateLimitAmountByUserIdAndName(user.getId(),
                                                                          category.getName(),
                                                                          new BigDecimal("12.34")));

        assertThat(updatedCategory).isNotEmpty();

        assertThat(updatedCategory.get())
                .usingRecursiveComparison(
                        RecursiveComparisonConfiguration.builder()
                                .withIgnoredFields("limitAmount")
                                .build())
                .isEqualTo(category);

        assertThat(updatedCategory.get().getLimitAmount()).isEqualTo(new BigDecimal("12.34"));

        assertThat(categoryRepository.findByUserId(user.getId()))
                .contains(updatedCategory.get());
    }

    @Test
    void update_alert_threshold() {
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

        var updatedCategory = tx(
                () -> categoryRepository.updateAlertThresholdByUserIdAndName(user.getId(),
                                                                             category.getName(),
                                                                             50));

        assertThat(updatedCategory).isNotEmpty();

        assertThat(updatedCategory.get())
                .usingRecursiveComparison(
                        RecursiveComparisonConfiguration.builder()
                                .withIgnoredFields("alertThreshold")
                                .build())
                .isEqualTo(category);

        assertThat(updatedCategory.get().getAlertThreshold()).isEqualTo(50);

        assertThat(categoryRepository.findByUserId(user.getId()))
                .contains(updatedCategory.get());
    }
}