package com.example.contentcrm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Content CRM Social Publishing MVP")
                .version("0.0.1")
                .description("Diploma MVP API for content production, approvals and publication fallback"));
    }
}
