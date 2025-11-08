package com.dfedorino.cashy.it;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.domain.model.transaction.TransactionTypes;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.service.CategoryService;
import com.dfedorino.cashy.service.UserService;
import com.dfedorino.cashy.service.dto.CategoryDto;
import com.dfedorino.cashy.util.PropertiesUtil;
import java.io.IOException;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CategoryScenariosIT {

    private AnnotationConfigApplicationContext ctx;

    private UserService userService;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() throws IOException {
        initContextWithProperties("test.properties");
        initBeans();

        DataSource dataSource = ctx.getBean(DataSource.class);
        DataUtil.preloadDataFromClasspath("schema.sql", dataSource);
        DataUtil.preloadDataFromClasspath("init.sql", dataSource);
    }

    @AfterEach
    void tearDown() {
        DataUtil.dropAllObjects(ctx.getBean(DataSource.class));
    }

    @Test
    void income_category_is_created() {
        userService.registerUser(TestConstants.LOGIN, TestConstants.PASSWORD);
        var createdCategory = categoryService.createIncomeCategory(TestConstants.INCOME_CATEGORY,
                                                                   new BigDecimal("10000.00"));

        assertThat(createdCategory.userLogin()).isEqualTo(TestConstants.LOGIN);
        assertThat(createdCategory.categoryName()).isEqualTo(TestConstants.INCOME_CATEGORY);
        assertThat(createdCategory.transactionType()).isEqualTo(TransactionTypes.INCOME);
        assertThat(createdCategory.limit()).isNull();
        assertThat(createdCategory.alertThreshold()).isNull();
        assertThat(createdCategory.currentBalance()).isEqualByComparingTo("10000.00");
        assertThat(createdCategory.remainingBalance()).isNull();
    }

    @Test
    void expense_category_is_created() {
        userService.registerUser(TestConstants.LOGIN, TestConstants.PASSWORD);
        var createdCategory = categoryService.createExpenseCategory(TestConstants.EXPENSE_CATEGORY,
                                                                    new BigDecimal("5000.00"),
                                                                    70);

        assertThat(createdCategory.userLogin()).isEqualTo(TestConstants.LOGIN);
        assertThat(createdCategory.categoryName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(createdCategory.transactionType()).isEqualTo(TransactionTypes.EXPENSE);
        assertThat(createdCategory.limit()).isEqualByComparingTo("5000.00");
        assertThat(createdCategory.alertThreshold()).isEqualTo(70);
        assertThat(createdCategory.currentBalance()).isZero();
        assertThat(createdCategory.remainingBalance()).isEqualTo("5000.00");
    }

    @Test
    void all_categories_found() {
        userService.registerUser(TestConstants.LOGIN, TestConstants.PASSWORD);
        var createdIncomeCategory = categoryService.createIncomeCategory(
                TestConstants.INCOME_CATEGORY,
                new BigDecimal("10000.00"));
        var createdExpenseCategory = categoryService.createExpenseCategory(
                TestConstants.EXPENSE_CATEGORY,
                new BigDecimal("5000.00"),
                70);

        assertThat(categoryService.findAllCategories())
                .usingRecursiveFieldByFieldElementComparator(
                        RecursiveComparisonConfiguration.builder()
                                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                                .build())
                .containsExactlyInAnyOrder(new CategoryDto(
                                                   TestConstants.LOGIN,
                                                   TestConstants.INCOME_CATEGORY,
                                                   TransactionTypes.INCOME,
                                                   null,
                                                   null,
                                                   createdIncomeCategory.currentBalance(),
                                                   null
                                           ), new CategoryDto(
                                                   TestConstants.LOGIN,
                                                   TestConstants.EXPENSE_CATEGORY,
                                                   TransactionTypes.EXPENSE,
                                                   createdExpenseCategory.limit(),
                                                   createdExpenseCategory.alertThreshold(),
                                                   createdExpenseCategory.currentBalance(),
                                                   createdExpenseCategory.remainingBalance()
                                           )
                );
    }

    private void initContextWithProperties(String path) throws IOException {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        PropertiesUtil.addApplicationProperties(ctx, path);
        ctx.refresh();
    }

    private void initBeans() {
        userService = ctx.getBean(UserService.class);
        categoryService = ctx.getBean(CategoryService.class);
    }

}
