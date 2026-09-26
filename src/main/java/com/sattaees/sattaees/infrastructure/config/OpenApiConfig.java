package com.sattaees.sattaees.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI sattaeesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SATTAEES - Labor Service Platform API")
                        .description("Production-grade modular monolith REST API documentation for the Sattaees service marketplace platform.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sattaees Engineering")
                                .email("engineering@sattaees.com"))
                        .license(new License().name("Apache 2.0").url("https://spring.io")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide a valid JWT Access Token in the Authorization header: Bearer <token>")));
    }
}
