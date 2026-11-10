
package com.madhav.incidentmanagement;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the production server URL displayed in Swagger UI.
 */
@Configuration
public class OpenApiConfig {

        /**
         * Returns the OpenAPI configuration with the HTTPS production URL.
         */
        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                        .addServersItem(
                                new Server()
                                        .url("https://incident-management-production.up.railway.app")
                                        .description("Production server")
                        );
        }
}
