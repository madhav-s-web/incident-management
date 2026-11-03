package com.madhav.incidentmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Configures authentication and authorization rules for the application.
@Configuration
public class SecurityConfig {

    // Allows registration, health checks, and Swagger documentation while protecting business APIs.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Allows the Swagger UI and its generated API documentation.
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        // Only ADMIN users can update incident status or assignment.
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/incidents/*/status"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/incidents/*/assign/*"
                        ).hasRole("ADMIN")

                        // All other endpoints still require authentication.
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}