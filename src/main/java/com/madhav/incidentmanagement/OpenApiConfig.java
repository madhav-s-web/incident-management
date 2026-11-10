
package com.madhav.incidentmanagement;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the production HTTPS server URL displayed in Swagger UI.
 */
@Configuration
public class OpenApiConfig {

        /**
         * Registers the production API server in the OpenAPI document.
         *
         * @return the configured OpenAPI instance
         */
        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                        .addServersItem(new Server()
                                .url("https://incident-management-production.up.railway.app")
                                .description("Production server"));
        }
}
