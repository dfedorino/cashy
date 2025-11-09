package com.dfedorino.cashy;

import com.dfedorino.cashy.config.AppConfig;
import com.dfedorino.cashy.jdbc.util.DataUtil;
import com.dfedorino.cashy.ui.cli.CommandLineInterface;
import com.dfedorino.cashy.ui.cli.command.Command;
import com.dfedorino.cashy.util.PropertiesUtil;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class CashyApp {

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(AppConfig.class);
            PropertiesUtil.addApplicationProperties(context, "application.properties");
            context.refresh();

            DataSource dataSource = context.getBean(DataSource.class);
            DataUtil.preloadDataFromClasspath("schema.sql", dataSource);
            try {
                DataUtil.preloadDataFromClasspath("init.sql", dataSource);
                log.info(">> Dictionaries initialized for the first time");
            } catch (Exception ignored) {
                log.info(">> Dictionaries initialized previously");
            }
            log.info(">> Database initialized");

            CommandLineInterface cli = context.getBean(CommandLineInterface.class);
            cli.start();
        } catch (Exception e) {
            log.error(">> Unexpected exception ", e);
        }
    }
}
