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

    // Allows public registration and health checks while protecting other APIs.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/incidents/*/status"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/incidents/*/assign/*")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
