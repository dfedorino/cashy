package com.dfedorino.cashy.jdbc.repository;

import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.jdbc.config.JdbcConfig;
import com.dfedorino.cashy.jdbc.connection.HikariConfiguration;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.util.PropertiesUtil;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;

public class AbstractJdbcRepositoryTestSkeleton {

    protected AnnotationConfigApplicationContext ctx;
    protected TransactionTemplate tx;

    @BeforeEach
    void setUp() throws IOException {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(JdbcConfig.class);
        PropertiesUtil.addApplicationProperties(ctx, "test.properties");
        ctx.refresh();

        DataUtil.preloadDataFromClasspath("schema.sql", ctx.getBean(DataSource.class));
        DataUtil.preloadDataFromClasspath("init.sql", ctx.getBean(DataSource.class));

        tx = ctx.getBean(TransactionTemplate.class);
    }

    @AfterEach
    void tearDown() {
        DataUtil.dropAllObjects(ctx.getBean(DataSource.class));
    }

    protected <T> T tx(Callable<T> workload) {
        return Objects.requireNonNull(tx.execute($ -> {
            try {
                return workload.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    protected void tx(Runnable workload) {
        tx.executeWithoutResult($ -> workload.run());
    }

}
