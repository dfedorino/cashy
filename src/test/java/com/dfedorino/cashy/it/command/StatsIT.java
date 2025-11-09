package com.dfedorino.cashy.it.command;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.scenario.dto.StatsDto;
import com.dfedorino.cashy.service.dto.ShortCategoryDto;
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
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class StatsIT {

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
    void stats_all_categories() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        income.apply(Income.KEY_TOKEN, TestConstants.INCOME_CATEGORY, "100");

        createCategory.apply(
                CreateCategory.KEY_TOKEN,
                TestConstants.EXPENSE_CATEGORY,
                TestConstants.LIMIT_AMOUNT.toPlainString(),
                CreateCategory.ALERT_TOKEN,
                TestConstants.ALERT_THRESHOLD + ""
        );

        withdraw.apply(Withdraw.KEY_TOKEN, TestConstants.EXPENSE_CATEGORY, "50");
        withdraw.apply(Withdraw.KEY_TOKEN, "taxi", "50");

        ResultWithNotification<StatsDto> statsResult = stats.apply(Stats.KEY_TOKEN);

        assertThat(statsResult.notification()).isEqualTo(Stats.SUCCESS_MESSAGE);
        assertThat(statsResult.result()).isPresent();

        StatsDto actualStats = statsResult.result().get();

        assertThat(actualStats.totalIncomeAmount()).isEqualByComparingTo("100");
        assertThat(actualStats.incomeCategories())
                .usingRecursiveFieldByFieldElementComparator(
                        RecursiveComparisonConfiguration.builder()
                                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                                .build())
                .containsExactly(
                        new ShortCategoryDto(TestConstants.INCOME_CATEGORY,
                                             new BigDecimal("100"),
                                             null,
                                             null)
                );
        assertThat(actualStats.totalExpenseAmount()).isEqualByComparingTo("100");
        assertThat(actualStats.expenseCategories())
                .usingRecursiveFieldByFieldElementComparator(
                        RecursiveComparisonConfiguration.builder()
                                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                                .build())
                .containsExactly(
                        new ShortCategoryDto(TestConstants.EXPENSE_CATEGORY,
                                             new BigDecimal("50"),
                                             TestConstants.LIMIT_AMOUNT,
                                             TestConstants.LIMIT_AMOUNT.subtract(
                                                     new BigDecimal("50"))),

                        new ShortCategoryDto("taxi",
                                             new BigDecimal("50"),
                                             null,
                                             null)
                );
    }

    @Test
    void stats_filtered_categories_success() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        income.apply(Income.KEY_TOKEN, TestConstants.INCOME_CATEGORY, "100");

        createCategory.apply(
                CreateCategory.KEY_TOKEN,
                TestConstants.EXPENSE_CATEGORY,
                TestConstants.LIMIT_AMOUNT.toPlainString(),
                CreateCategory.ALERT_TOKEN,
                TestConstants.ALERT_THRESHOLD + ""
        );

        withdraw.apply(Withdraw.KEY_TOKEN, TestConstants.EXPENSE_CATEGORY, "50");
        withdraw.apply(Withdraw.KEY_TOKEN, "taxi", "50");

        ResultWithNotification<StatsDto> filteredStatsResult = stats.apply(Stats.KEY_TOKEN,
                                                                           Stats.CATEGORIES_TOKEN,
                                                                           TestConstants.EXPENSE_CATEGORY);

        assertThat(filteredStatsResult.notification()).isEqualTo(Stats.SUCCESS_MESSAGE);
        assertThat(filteredStatsResult.result()).isPresent();

        log.info(">> stats: {}", filteredStatsResult.result().get());
    }

    @Test
    void stats_filtered_categories_partial() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        income.apply(Income.KEY_TOKEN, TestConstants.INCOME_CATEGORY, "100");

        createCategory.apply(
                CreateCategory.KEY_TOKEN,
                TestConstants.EXPENSE_CATEGORY,
                TestConstants.LIMIT_AMOUNT.toPlainString(),
                CreateCategory.ALERT_TOKEN,
                TestConstants.ALERT_THRESHOLD + ""
        );

        withdraw.apply(Withdraw.KEY_TOKEN, TestConstants.EXPENSE_CATEGORY, "50");
        withdraw.apply(Withdraw.KEY_TOKEN, "taxi", "50");

        ResultWithNotification<StatsDto> filteredStatsResult = stats.apply(Stats.KEY_TOKEN,
                                                                           Stats.CATEGORIES_TOKEN,
                                                                           TestConstants.EXPENSE_CATEGORY,
                                                                           "entertainment");

        assertThat(filteredStatsResult.notification()).isEqualTo(Stats.NOT_ALL_CATEGORIES_FOUND);
        assertThat(filteredStatsResult.result()).isPresent();

        log.info(">> stats: {}", filteredStatsResult.result().get());
    }

    @Test
    void stats_filtered_categories_none() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        income.apply(Income.KEY_TOKEN, TestConstants.INCOME_CATEGORY, "100");

        createCategory.apply(
                CreateCategory.KEY_TOKEN,
                TestConstants.EXPENSE_CATEGORY,
                TestConstants.LIMIT_AMOUNT.toPlainString(),
                CreateCategory.ALERT_TOKEN,
                TestConstants.ALERT_THRESHOLD + ""
        );

        withdraw.apply(Withdraw.KEY_TOKEN, TestConstants.EXPENSE_CATEGORY, "50");
        withdraw.apply(Withdraw.KEY_TOKEN, "taxi", "50");

        ResultWithNotification<StatsDto> filteredStatsResult = stats.apply(Stats.KEY_TOKEN,
                                                                           Stats.CATEGORIES_TOKEN,
                                                                           "entertainment");

        assertThat(filteredStatsResult.notification()).isEqualTo(Stats.FAILED_TO_FIND_CATEGORIES + "[entertainment]");
        assertThat(filteredStatsResult.result()).isEmpty();
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
