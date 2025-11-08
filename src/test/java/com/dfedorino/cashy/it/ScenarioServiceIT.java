package com.dfedorino.cashy.it;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.scenario.ScenarioService;
import com.dfedorino.cashy.scenario.dto.StatsDto;
import com.dfedorino.cashy.service.dto.ShortCategoryDto;
import com.dfedorino.cashy.util.PropertiesUtil;
import java.io.IOException;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ScenarioServiceIT {

    private AnnotationConfigApplicationContext ctx;

    private ScenarioService scenarioService;

    @BeforeEach
    void setUp() throws IOException {
        initContextWithProperties();
        scenarioService = ctx.getBean(ScenarioService.class);

        DataSource dataSource = ctx.getBean(DataSource.class);
        DataUtil.preloadDataFromClasspath("schema.sql", dataSource);
        DataUtil.preloadDataFromClasspath("init.sql", dataSource);
    }

    @AfterEach
    void tearDown() {
        DataUtil.dropAllObjects(ctx.getBean(DataSource.class));
    }

    @Test
    void happy_path() {
        scenarioService.login(TestConstants.LOGIN, TestConstants.PASSWORD);

        // Budget
        scenarioService.budget("Еда", new BigDecimal("4000"), null);
        scenarioService.budget("Развлечения", new BigDecimal("3000"), null);
        scenarioService.budget("Коммунальные услуги", new BigDecimal("2500"), null);

        // Expenses
        scenarioService.withdraw("Еда", new BigDecimal("300"));
        scenarioService.withdraw("Еда", new BigDecimal("500"));
        scenarioService.withdraw("Развлечения", new BigDecimal("3000"));
        scenarioService.withdraw("Коммунальные услуги", new BigDecimal("3000"));
        scenarioService.withdraw("Такси", new BigDecimal("1500"));

        // Incomes
        scenarioService.topUp("Зарплата", new BigDecimal("20000"));
        scenarioService.topUp("Зарплата", new BigDecimal("40000"));
        scenarioService.topUp("Бонус", new BigDecimal("3000"));

        assertThat(scenarioService.stats().isSuccess()).isTrue();
        assertThat(scenarioService.stats().result()).isPresent();
        StatsDto actualStats = scenarioService.stats().result().get();

        assertThat(actualStats.totalIncomeAmount())
                .isEqualByComparingTo(new BigDecimal("63000.00"));

        assertThat(actualStats.incomeCategories())
                .containsExactlyInAnyOrder(
                        new ShortCategoryDto("Зарплата", new BigDecimal("60000.00"), null, null),
                        new ShortCategoryDto("Бонус", new BigDecimal("3000.00"), null, null)
                );

        assertThat(actualStats.totalExpenseAmount())
                .isEqualByComparingTo(new BigDecimal("8300.00"));

        assertThat(actualStats.expenseCategories())
                .containsExactlyInAnyOrder(
                        new ShortCategoryDto("Коммунальные услуги", new BigDecimal("3000.00"), new BigDecimal("2500.00"), new BigDecimal("-500.00")),
                        new ShortCategoryDto("Еда", new BigDecimal("800.00"), new BigDecimal("4000.00"), new BigDecimal("3200.00")),
                        new ShortCategoryDto("Развлечения", new BigDecimal("3000.00"), new BigDecimal("3000.00"), new BigDecimal("0.00")),
                        new ShortCategoryDto("Такси", new BigDecimal("1500.00"), null, null)
                );
    }

    private void initContextWithProperties() throws IOException {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        PropertiesUtil.addApplicationProperties(ctx, "test.properties");
        ctx.refresh();
    }

}
