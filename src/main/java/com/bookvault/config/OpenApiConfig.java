package com.bookvault.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation configuration for BookVault API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI metadata displayed in Swagger UI.
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI bookVaultOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo());
    }

    private Info buildApiInfo() {
        return new Info()
                .title("BookVault Library Management API")
                .description("RESTful API for managing library books, members, borrowing, and fines.")
                .version("1.0.0")
                .contact(buildContact())
                .license(buildLicense());
    }

    private Contact buildContact() {
        return new Contact()
                .name("BookVault Team")
                .email("support@bookvault.com");
    }

    private License buildLicense() {
        return new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");
    }
}
