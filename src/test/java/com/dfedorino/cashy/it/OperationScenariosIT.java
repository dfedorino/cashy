package com.dfedorino.cashy.it;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.service.CategoryService;
import com.dfedorino.cashy.service.OperationService;
import com.dfedorino.cashy.service.UserService;
import com.dfedorino.cashy.util.PropertiesUtil;
import java.io.IOException;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class OperationScenariosIT {

    private AnnotationConfigApplicationContext ctx;

    private UserService userService;
    private CategoryService categoryService;
    private OperationService operationService;

    @BeforeEach
    void setUp() throws IOException {
        initContextWithProperties();
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
    void income_operation() {
        userService.registerUser(TestConstants.LOGIN, TestConstants.PASSWORD);
        categoryService.createIncomeCategory(TestConstants.INCOME_CATEGORY,
                                             BigDecimal.ZERO);
        BigDecimal operationAmount = new BigDecimal("20000.00");
        var operationDto = operationService.createIncomeOperation(operationAmount,
                                                                  TestConstants.INCOME_CATEGORY);

        assertThat(operationDto.userLogin()).isEqualTo(TestConstants.LOGIN);
        assertThat(operationDto.categoryName()).isEqualTo(TestConstants.INCOME_CATEGORY);
        assertThat(operationDto.operationAmount()).isEqualByComparingTo(operationAmount);
        assertThat(operationDto.categoryBalanceAfterOperation())
                .isEqualByComparingTo(operationAmount);
        assertThat(operationDto.categoryRemainingLimitAfterOperation())
                .isNull();
        assertThat(operationDto.totalBalanceAfterOperation())
                .isEqualByComparingTo(operationAmount);
    }

    @Test
    void expense_operation_after_income_operation() {
        userService.registerUser(TestConstants.LOGIN, TestConstants.PASSWORD);
        categoryService.createIncomeCategory(TestConstants.INCOME_CATEGORY,
                                             BigDecimal.ZERO);

        BigDecimal incomeBalanceOperation = new BigDecimal("20000.00");
        operationService.createIncomeOperation(incomeBalanceOperation,
                                               TestConstants.INCOME_CATEGORY);

        BigDecimal categoryLimit = new BigDecimal("4000");
        categoryService.createExpenseCategory(TestConstants.EXPENSE_CATEGORY,
                                              categoryLimit,
                                              50);

        BigDecimal operationAmount = new BigDecimal("300");
        var operationDto = operationService.createExpenseOperation(operationAmount,
                                                                   TestConstants.EXPENSE_CATEGORY);

        assertThat(operationDto.userLogin()).isEqualTo(TestConstants.LOGIN);
        assertThat(operationDto.categoryName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(operationDto.operationAmount()).isEqualByComparingTo(operationAmount);
        assertThat(operationDto.categoryBalanceAfterOperation())
                .isEqualByComparingTo(operationAmount);
        assertThat(operationDto.categoryRemainingLimitAfterOperation())
                .isEqualByComparingTo(categoryLimit.subtract(operationAmount));
        assertThat(operationDto.totalBalanceAfterOperation())
                .isEqualByComparingTo(incomeBalanceOperation.subtract(operationAmount));
    }

    @Test
    void several_categories_operations() {
        userService.registerUser(TestConstants.LOGIN, TestConstants.PASSWORD);

        // several categories expense operations
        categoryService.createExpenseCategory("еда",
                                              new BigDecimal("4000"),
                                              80);
        operationService.createExpenseOperation(new BigDecimal("300"),
                                                "еда");
        var lastFoodOperation = operationService.createExpenseOperation(new BigDecimal("500"),
                                                                        "еда");

        assertThat(lastFoodOperation.userLogin()).isEqualTo(TestConstants.LOGIN);
        assertThat(lastFoodOperation.categoryName()).isEqualTo("еда");
        assertThat(lastFoodOperation.operationAmount())
                .isEqualByComparingTo(new BigDecimal("500"));
        assertThat(lastFoodOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("800"));
        assertThat(lastFoodOperation.categoryRemainingLimitAfterOperation())
                .isEqualByComparingTo(new BigDecimal("3200"));
        assertThat(lastFoodOperation.totalBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("-800"));

        categoryService.createExpenseCategory("развлечения",
                                              new BigDecimal("3000"),
                                              80);
        var lastEntertainmentOperation =
                operationService.createExpenseOperation(new BigDecimal("3000"),
                                                        "развлечения");

        assertThat(lastEntertainmentOperation.userLogin())
                .isEqualTo(TestConstants.LOGIN);
        assertThat(lastEntertainmentOperation.categoryName())
                .isEqualTo("развлечения");
        assertThat(lastEntertainmentOperation.operationAmount())
                .isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(lastEntertainmentOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(lastEntertainmentOperation.categoryRemainingLimitAfterOperation())
                .isZero();
        assertThat(lastEntertainmentOperation.totalBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("-3800"));

        categoryService.createExpenseCategory("коммунальные услуги",
                                              new BigDecimal("2500"),
                                              80);
        var lastUtilityServicesOperation =
                operationService.createExpenseOperation(new BigDecimal("3000"),
                                                        "коммунальные услуги");

        assertThat(lastUtilityServicesOperation.userLogin())
                .isEqualTo(TestConstants.LOGIN);
        assertThat(lastUtilityServicesOperation.categoryName())
                .isEqualTo("коммунальные услуги");
        assertThat(lastUtilityServicesOperation.operationAmount())
                .isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(lastUtilityServicesOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(lastUtilityServicesOperation.categoryRemainingLimitAfterOperation())
                .isEqualByComparingTo(new BigDecimal("-500"));
        assertThat(lastUtilityServicesOperation.totalBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("-6800"));

        // several income operations
        categoryService.createIncomeCategory("зарплата",
                                             BigDecimal.ZERO);
        operationService.createIncomeOperation(new BigDecimal("20000"),
                                               "зарплата");
        var lastWagesOperation = operationService.createIncomeOperation(new BigDecimal("40000"),
                                                                        "зарплата");

        assertThat(lastWagesOperation.userLogin())
                .isEqualTo(TestConstants.LOGIN);
        assertThat(lastWagesOperation.categoryName())
                .isEqualTo("зарплата");
        assertThat(lastWagesOperation.operationAmount())
                .isEqualByComparingTo(new BigDecimal("40000"));
        assertThat(lastWagesOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(lastWagesOperation.categoryRemainingLimitAfterOperation())
                .isNull();
        assertThat(lastWagesOperation.totalBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("53200"));

        categoryService.createIncomeCategory("бонус",
                                             BigDecimal.ZERO);
        var lastBonusOperation = operationService.createIncomeOperation(new BigDecimal("3000"),
                                                                        "бонус");

        assertThat(lastBonusOperation.userLogin())
                .isEqualTo(TestConstants.LOGIN);
        assertThat(lastBonusOperation.categoryName())
                .isEqualTo("бонус");
        assertThat(lastBonusOperation.operationAmount())
                .isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(lastBonusOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(lastBonusOperation.categoryRemainingLimitAfterOperation())
                .isNull();
        assertThat(lastBonusOperation.totalBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("56200"));

        // expense operation without category
        var lastTaxiOperation = operationService.createExpenseOperation(new BigDecimal("1500"));

        assertThat(lastTaxiOperation.userLogin())
                .isEqualTo(TestConstants.LOGIN);
        assertThat(lastTaxiOperation.categoryName())
                .isNull();
        assertThat(lastTaxiOperation.operationAmount())
                .isEqualByComparingTo(new BigDecimal("1500"));
        assertThat(lastTaxiOperation.categoryBalanceAfterOperation())
                .isNull();
        assertThat(lastTaxiOperation.categoryRemainingLimitAfterOperation())
                .isNull();
        assertThat(lastTaxiOperation.totalBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("54700"));
    }

    private void initContextWithProperties() throws IOException {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        PropertiesUtil.addApplicationProperties(ctx, "test.properties");
        ctx.refresh();
    }

    private void initBeans() {
        userService = ctx.getBean(UserService.class);
        categoryService = ctx.getBean(CategoryService.class);
        operationService = ctx.getBean(OperationService.class);
    }

}
