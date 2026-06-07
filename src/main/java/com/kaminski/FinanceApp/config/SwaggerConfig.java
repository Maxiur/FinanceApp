package com.kaminski.FinanceApp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI financeAppOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Finance App API")
                        .description("Aplikacja do zarządzania budżetem")
                        .version("v1.0"));
    }
}
