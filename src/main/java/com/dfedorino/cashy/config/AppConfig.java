package com.dfedorino.cashy.config;

import com.dfedorino.cashy.CashyApp;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = CashyApp.class)
public class AppConfig {

}
