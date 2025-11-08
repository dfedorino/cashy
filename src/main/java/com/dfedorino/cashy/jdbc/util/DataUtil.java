package com.dfedorino.cashy.jdbc.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@UtilityClass
public class DataUtil {

    public void preloadDataFromClasspath(String script, DataSource dataSource) {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(new ClassPathResource(script));
        resourceDatabasePopulator.execute(dataSource);
    }

    public void dropAllObjects(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("DROP ALL OBJECTS");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
