package com.nominal.lynx.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary security configuration for bootstrap stage.
 * <p>
 * All endpoints are open except CSRF is disabled for local development convenience.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Registers the security filter chain used by Spring Security.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/actuator/**").permitAll()
                        .anyRequest().permitAll());

        return http.build();
    }
}
