package com.dfedorino.cashy.it.command;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.service.dto.CategoryDto;
import com.dfedorino.cashy.service.dto.OperationDto;
import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import com.dfedorino.cashy.ui.cli.command.impl.CreateCategory;
import com.dfedorino.cashy.ui.cli.command.impl.Login;
import com.dfedorino.cashy.ui.cli.command.impl.Withdraw;
import com.dfedorino.cashy.util.PropertiesUtil;
import java.io.IOException;
import java.math.BigDecimal;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class WithdrawIT {

    private AnnotationConfigApplicationContext ctx;

    private Login login;
    private CreateCategory createCategory;
    private Withdraw withdraw;

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
    void withdrawal_with_budget() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        ResultWithNotification<CategoryDto> categoryResult = createCategory.apply(
                CreateCategory.KEY_TOKEN,
                TestConstants.EXPENSE_CATEGORY,
                TestConstants.LIMIT_AMOUNT.toPlainString(),
                CreateCategory.ALERT_TOKEN,
                TestConstants.ALERT_THRESHOLD + ""
        );

        assertThat(categoryResult.notification()).isEqualTo(CreateCategory.SUCCESS_MESSAGE);
        assertThat(categoryResult.result()).isNotEmpty();
        CategoryDto actualCategory = categoryResult.result().get();

        assertThat(actualCategory.categoryName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(actualCategory.limit()).isEqualByComparingTo(TestConstants.LIMIT_AMOUNT);
        assertThat(actualCategory.alertThreshold())
                .isEqualByComparingTo(TestConstants.ALERT_THRESHOLD);

        ResultWithNotification<OperationDto> withdrawalResult =
                withdraw.apply(Withdraw.KEY_TOKEN,
                               TestConstants.EXPENSE_CATEGORY,
                               "100");

        assertThat(withdrawalResult.notification()).isEqualTo(Withdraw.SUCCESS_MESSAGE);
        assertThat(withdrawalResult.result()).isNotEmpty();
        OperationDto actualOperation = withdrawalResult.result().get();
        assertThat(actualOperation.categoryName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(actualOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo("100");
        assertThat(actualOperation.categoryRemainingLimitAfterOperation())
                .isEqualByComparingTo(TestConstants.LIMIT_AMOUNT.subtract(new BigDecimal("100")));
    }

    @Test
    void withdrawal_without_budget() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        ResultWithNotification<OperationDto> withdrawalResult =
                withdraw.apply(Withdraw.KEY_TOKEN,
                               TestConstants.EXPENSE_CATEGORY,
                               "100");

        assertThat(withdrawalResult.notification()).isEqualTo(Withdraw.SUCCESS_MESSAGE);
        assertThat(withdrawalResult.result()).isNotEmpty();
        OperationDto actualOperation = withdrawalResult.result().get();
        assertThat(actualOperation.categoryName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(actualOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo("100");
        assertThat(actualOperation.categoryRemainingLimitAfterOperation())
                .isNull();
    }

    @Test
    void withdrawal_exceed_alert_threshold() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        createCategory.apply(CreateCategory.KEY_TOKEN,
                             TestConstants.EXPENSE_CATEGORY,
                             "100",
                             CreateCategory.ALERT_TOKEN,
                             "80");

        ResultWithNotification<OperationDto> withdrawalResult =
                withdraw.apply(Withdraw.KEY_TOKEN,
                               TestConstants.EXPENSE_CATEGORY,
                               "80");

        assertThat(withdrawalResult.notification())
                .contains(Withdraw.SUCCESS_MESSAGE)
                .contains(Withdraw.THRESHOLD_REACHED.formatted("80"));

        assertThat(withdrawalResult.result()).isNotEmpty();
        OperationDto actualOperation = withdrawalResult.result().get();
        assertThat(actualOperation.categoryName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(actualOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("80"));
        assertThat(actualOperation.categoryRemainingLimitAfterOperation())
                .isEqualByComparingTo(new BigDecimal("20"));
    }

    @Test
    void withdrawal_category_budget_is_zero() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        createCategory.apply(CreateCategory.KEY_TOKEN,
                             TestConstants.EXPENSE_CATEGORY,
                             "100",
                             CreateCategory.ALERT_TOKEN,
                             "80");

        ResultWithNotification<OperationDto> withdrawalResult =
                withdraw.apply(Withdraw.KEY_TOKEN,
                               TestConstants.EXPENSE_CATEGORY,
                               "100");

        assertThat(withdrawalResult.notification())
                .contains(Withdraw.SUCCESS_MESSAGE)
                .contains(Withdraw.CATEGORY_LIMIT_REACHED);

        assertThat(withdrawalResult.result()).isNotEmpty();
        OperationDto actualOperation = withdrawalResult.result().get();
        assertThat(actualOperation.categoryName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(actualOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("100"));
        assertThat(actualOperation.categoryRemainingLimitAfterOperation())
                .isZero();
    }

    @Test
    void withdrawal_category_budget_is_exceeded() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        createCategory.apply(CreateCategory.KEY_TOKEN,
                             TestConstants.EXPENSE_CATEGORY,
                             "100",
                             CreateCategory.ALERT_TOKEN,
                             "80");

        ResultWithNotification<OperationDto> withdrawalResult =
                withdraw.apply(Withdraw.KEY_TOKEN,
                               TestConstants.EXPENSE_CATEGORY,
                               "100.01");

        assertThat(withdrawalResult.notification())
                .contains(Withdraw.SUCCESS_MESSAGE)
                .contains(Withdraw.CATEGORY_LIMIT_EXCEEDED);

        assertThat(withdrawalResult.result()).isNotEmpty();
        OperationDto actualOperation = withdrawalResult.result().get();
        assertThat(actualOperation.categoryName()).isEqualTo(TestConstants.EXPENSE_CATEGORY);
        assertThat(actualOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo(new BigDecimal("100.01"));
        assertThat(actualOperation.categoryRemainingLimitAfterOperation())
                .isEqualByComparingTo(new BigDecimal("-0.01"));
    }

    private void initContextWithProperties() throws IOException {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        PropertiesUtil.addApplicationProperties(ctx, "test.properties");
        ctx.refresh();
    }

    private void initBeans() {
        login = ctx.getBean(Login.class);
        createCategory = ctx.getBean(CreateCategory.class);
        withdraw = ctx.getBean(Withdraw.class);
    }

}
