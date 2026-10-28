package com.sentinel.ledger.config;

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

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Project EduTrust (Sentinel-Ledger) API")
                        .version("1.0.0")
                        .description("APAAR ID-Based Student Data Management and Verification Platform for AICTE-Approved Institutions.\n\n" +
                                "Supports institutional data import, cross-institution duplicate candidate detection, " +
                                "approved intake compliance, tamper-resistant digital credential verification, and audit logs.")
                        .contact(new Contact().name("SIH Development Team").email("support@edutrust.gov.in"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token in the format: Bearer <token>")));
    }

    @Bean
    public org.springdoc.webmvc.ui.SwaggerIndexTransformer swaggerIndexTransformer(
            org.springdoc.core.properties.SwaggerUiConfigProperties swaggerUiConfig,
            org.springdoc.core.properties.SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            org.springdoc.core.properties.SwaggerUiConfigParameters swaggerUiConfigParameters,
            org.springdoc.webmvc.ui.SwaggerWelcomeCommon swaggerWelcomeCommon,
            org.springdoc.core.providers.ObjectMapperProvider objectMapperProvider) {
        return new CustomSwaggerIndexTransformer(
                swaggerUiConfig,
                swaggerUiOAuthProperties,
                swaggerUiConfigParameters,
                swaggerWelcomeCommon,
                objectMapperProvider
        );
    }
}

