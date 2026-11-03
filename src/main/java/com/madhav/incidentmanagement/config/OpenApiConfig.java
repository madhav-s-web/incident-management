package com.madhav.incidentmanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Configures Swagger documentation and HTTP Basic Authentication.
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Incident Management API",
                version = "v1",
                description = "REST APIs for managing incidents, services, and audit history."
        ),
        security = @SecurityRequirement(name = "basicAuth")
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Enter your registered username and password."
)
public class OpenApiConfig {
}