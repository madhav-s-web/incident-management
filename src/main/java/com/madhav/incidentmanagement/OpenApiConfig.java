package com.madhav.incidentmanagement;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Swagger to use the deployed HTTPS API URL on Railway.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Sets the production server URL displayed in Swagger UI.
     *
     * @return the configured OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url("https://incident-management-production.up.railway.app"));
    }
}