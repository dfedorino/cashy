package com.dfedorino.cashy.it.command;

import static org.assertj.core.api.Assertions.assertThat;
import com.dfedorino.cashy.TestConstants;
import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.service.dto.CategoryDto;
import com.dfedorino.cashy.ui.cli.command.dto.ResultWithNotification;
import com.dfedorino.cashy.ui.cli.command.impl.CreateCategory;
import com.dfedorino.cashy.ui.cli.command.impl.EditCategory;
import com.dfedorino.cashy.ui.cli.command.impl.Login;
import com.dfedorino.cashy.util.PropertiesUtil;
import java.io.IOException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class EditCategoryIT {

    private AnnotationConfigApplicationContext ctx;

    private Login login;
    private CreateCategory createCategory;
    private EditCategory editCategory;

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
    void name_edited() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        createCategory.apply(CreateCategory.KEY_TOKEN,
                             "food",
                             "30000",
                             CreateCategory.ALERT_TOKEN,
                             "80");

        ResultWithNotification<CategoryDto> edited = editCategory.apply(EditCategory.KEY_TOKEN,
                                                                        "food",
                                                                        EditCategory.NAME_TOKEN,
                                                                        "groceries");

        assertThat(edited.notification()).isEqualTo(EditCategory.SUCCESS_MESSAGE);
        assertThat(edited.result()).isNotEmpty();
        CategoryDto actualEditedCategory = edited.result().get();

        assertThat(actualEditedCategory.categoryName()).isEqualTo("groceries");
        assertThat(actualEditedCategory.limit()).isEqualByComparingTo("30000");
    }

    @Test
    void limit_edited() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        createCategory.apply(CreateCategory.KEY_TOKEN,
                             "food",
                             "30000",
                             CreateCategory.ALERT_TOKEN,
                             "80");

        ResultWithNotification<CategoryDto> edited = editCategory.apply(EditCategory.KEY_TOKEN,
                                                                        "food",
                                                                        EditCategory.LIMIT_TOKEN,
                                                                        "20000");

        assertThat(edited.notification()).isEqualTo(EditCategory.SUCCESS_MESSAGE);
        assertThat(edited.result()).isNotEmpty();
        CategoryDto actualEditedCategory = edited.result().get();

        assertThat(actualEditedCategory.categoryName()).isEqualTo("food");
        assertThat(actualEditedCategory.limit()).isEqualByComparingTo("20000");
    }

    @Test
    void editing_non_existing_category() {
        login.apply(Login.KEY_TOKEN, TestConstants.LOGIN,
                    Login.PASSWORD_TOKEN, TestConstants.PASSWORD);

        ResultWithNotification<CategoryDto> edited = editCategory.apply(EditCategory.KEY_TOKEN,
                                                                        "food",
                                                                        EditCategory.LIMIT_TOKEN,
                                                                        "20000");

        assertThat(edited.notification()).isEqualTo("Category not found by name: food");
        assertThat(edited.result()).isEmpty();
    }

    private void initContextWithProperties(String path) throws IOException {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        PropertiesUtil.addApplicationProperties(ctx, path);
        ctx.refresh();
    }

    private void initBeans() {
        login = ctx.getBean(Login.class);
        createCategory = ctx.getBean(CreateCategory.class);
        editCategory = ctx.getBean(EditCategory.class);
    }

}
