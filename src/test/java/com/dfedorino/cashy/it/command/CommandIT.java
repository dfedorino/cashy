package com.dfedorino.cashy.it.command;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.scenario.dto.StatsDto;
import com.dfedorino.cashy.service.dto.CategoryDto;
import com.dfedorino.cashy.service.dto.OperationDto;
import com.dfedorino.cashy.service.dto.ShortCategoryDto;
import com.dfedorino.cashy.ui.cli.command.Command;
import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import com.dfedorino.cashy.ui.cli.command.impl.CreateCategory;
import com.dfedorino.cashy.ui.cli.command.impl.Income;
import com.dfedorino.cashy.ui.cli.command.impl.Login;
import com.dfedorino.cashy.ui.cli.command.impl.Stats;
import com.dfedorino.cashy.ui.cli.command.impl.Withdraw;
import com.dfedorino.cashy.util.PropertiesUtil;
import java.io.IOException;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CommandIT {

    private AnnotationConfigApplicationContext ctx;

    private Login login;
    private Income income;
    private CreateCategory createCategory;
    private Withdraw withdraw;
    private Stats stats;

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
    void successful_login() {
        ResultWithNotification<String> result = login.apply(Login.KEY_TOKEN,
                                                            TestConstants.LOGIN,
                                                            Login.PASSWORD_TOKEN,
                                                            TestConstants.PASSWORD);

        assertThat(result.notification()).contains(Login.SUCCESS_MESSAGE);
        assertThat(result.result()).contains(TestConstants.LOGIN);

    }

    @Test
    void wrong_password() {
        ResultWithNotification<String> result1 = login.apply(Login.KEY_TOKEN,
                                                             TestConstants.LOGIN,
                                                             Login.PASSWORD_TOKEN,
                                                             TestConstants.PASSWORD);

        assertThat(result1.result()).contains(TestConstants.LOGIN);

        ResultWithNotification<String> result2 = login.apply(Login.KEY_TOKEN,
                                                             TestConstants.LOGIN,
                                                             Login.PASSWORD_TOKEN,
                                                             "batman");

        assertThat(result2.notification())
                .contains("Password incorrect!");

        assertThat(result2.result()).isEmpty();

    }

    @Test
    void income_successful() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        ResultWithNotification<OperationDto> resultWithNotification =
                income.apply(Income.KEY_TOKEN,
                             TestConstants.INCOME_CATEGORY,
                             "100000");

        assertThat(resultWithNotification.notification()).isEqualTo(Income.SUCCESS_MESSAGE);
        assertThat(resultWithNotification.result()).isNotEmpty();
        OperationDto actualOperation = resultWithNotification.result().get();
        assertThat(actualOperation.categoryName()).isEqualTo(TestConstants.INCOME_CATEGORY);
        assertThat(actualOperation.categoryBalanceAfterOperation())
                .isEqualByComparingTo("100000");
    }

    @Test
    void income_failed() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        ResultWithNotification<OperationDto> resultWithNotification =
                income.apply(Income.KEY_TOKEN,
                             TestConstants.INCOME_CATEGORY,
                             "oops");

        assertThat(resultWithNotification.notification())
                .isEqualTo(Command.AMOUNT_INVALID_MESSAGE.formatted("oops"));
        assertThat(resultWithNotification.result()).isEmpty();
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

    private void initContextWithProperties(String path) throws IOException {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        PropertiesUtil.addApplicationProperties(ctx, path);
        ctx.refresh();
    }

    private void initBeans() {
        login = ctx.getBean(Login.class);
        income = ctx.getBean(Income.class);
        createCategory = ctx.getBean(CreateCategory.class);
        withdraw = ctx.getBean(Withdraw.class);
        stats = ctx.getBean(Stats.class);
    }

}
