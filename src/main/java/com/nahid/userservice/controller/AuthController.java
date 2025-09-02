package com.nahid.userservice.controller;

import com.nahid.userservice.dto.AuthRequest;
import com.nahid.userservice.dto.AuthResponse;
import com.nahid.userservice.dto.RefreshTokenRequest;
import com.nahid.userservice.dto.RegisterRequest;
import com.nahid.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller handling user registration, login, and token refresh
 *
 * Endpoints:
 * - POST /api/auth/register - User registration
 * - POST /api/auth/login - User authentication
 * - POST /api/auth/refresh - Token refresh
 */
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     *
     * What: Creates new user account and returns JWT tokens
     * Why: Enables new users to access the application
     * How: Validates input, creates user, generates tokens
     * When: Called during user signup process
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user and return tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token

     * What: Generates new access and refresh tokens
     * Why: Allows seamless token renewal without re-authentication
     * How: Validates refresh token, rotates it, returns new tokens
     * When: Called when access token expires (typically by frontend interceptors)
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }
}