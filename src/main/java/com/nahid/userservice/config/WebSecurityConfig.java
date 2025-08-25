package com.nahid.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection for simplicity
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/","/register","/login").permitAll() // Allow public access to these endpoints
                        .anyRequest().authenticated() // Require authentication for all other requests
                )
                .httpBasic(Customizer.withDefaults()); // Enable HTTP Basic authentication
        // Session management means that the server does not store any session information about the user
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Use stateless session management for REST APIs
        // Disable frame options to allow embedding in iframes (use with caution)
        http.headers(headers -> headers.frameOptions(
                HeadersConfigurer.FrameOptionsConfig::disable));
        // Disable CORS for simplicity, enable with proper
        return http.build();
    }


}
