package com.nahid.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class SwaggerUiOpener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port}")
    private String serverPort;

    @Value("${springdoc.swagger-ui.path}")
    private String swaggerPath;

    @Override
    public void onApplicationEvent( ApplicationReadyEvent event) {
        String url = "http://localhost:" + serverPort + swaggerPath;
        //System.out.println("Swagger UI available at: " + url);

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Auto-opening browser not supported on this platform");
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Failed to open browser: " + e.getMessage());
        }
    }

    public UserDetailsService userDetailsService() {
        return username -> {
            // In a real application, you would fetch user details from a database or another source
            if ("user".equals(username)) {
                return org.springframework.security.core.userdetails.User
                        .withUsername("user")
                        .password("{noop}password") // {noop} indicates that no encoding is used
                        .roles("USER")
                        .build();
            } else {
                throw new RuntimeException("User not found");
            }
        };
    }
}