package com.nahid.userservice.service;

import com.nahid.userservice.dto.AuthRequest;
import com.nahid.userservice.dto.AuthResponse;
import com.nahid.userservice.dto.RefreshTokenRequest;
import com.nahid.userservice.dto.RegisterRequest;
import com.nahid.userservice.entity.RefreshToken;
import com.nahid.userservice.entity.User;
import com.nahid.userservice.enums.Role;
import com.nahid.userservice.exception.AuthenticationException;
import com.nahid.userservice.exception.ResourceNotFoundException;
import com.nahid.userservice.repository.RefreshTokenRepository;
import com.nahid.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication Service handling user registration, login, and token refresh.
 * Why @Transactional:
 * - Ensures data consistency during user registration
 * - Rollback on any failure during token operations
 * - Manages refresh token cleanup atomically
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Registers a new user
     * What: Creates new user account with encrypted password
     * Why: Secure user onboarding with proper validation
     * How: Validates uniqueness, encrypts password, generates tokens
     * When: Called when new users sign up
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) throws AuthenticationException {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate tokens
        return generateTokenResponse(savedUser);
    }

    /**
     * Authenticates user and returns tokens
     */
    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        log.info("User authenticated successfully: {}", user.getEmail());

        // Revoke existing refresh tokens for security
        refreshTokenRepository.revokeAllByUser(user);

        return generateTokenResponse(user);
    }

    /**
     * Refreshes access token using refresh token
     * What: Generates new access and refresh tokens
     * Why: Allows seamless user experience while maintaining security
     * How: Validates refresh token, rotates it, generates new tokens
     * When: Called when access token expires
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Attempting token refresh");

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        // Validate refresh token
        if (refreshToken.isRevoked()) {
            log.warn("Attempted use of revoked refresh token");
            throw new AuthenticationException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            log.warn("Attempted use of expired refresh token");
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token has expired");
        }

        User user = refreshToken.getUser();

        // Revoke old refresh token (token rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed successfully for user: {}", user.getEmail());

        return generateTokenResponse(user);
    }

    /**
     * Generates complete token response (access + refresh tokens)
     */
    private AuthResponse generateTokenResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = generateRefreshTokenValue();

        // Save refresh token to database
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(900) // 15 minutes in seconds
                .build();
    }

    /**
     * Generates cryptographically secure refresh token
     */
    private String generateRefreshTokenValue() {
        return UUID.randomUUID().toString();
    }

    /**
     * Revokes all user's refresh tokens (useful for logout)
     */
    @Transactional
    public void revokeAllUserTokens(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);
        log.info("All refresh tokens revoked for user: {}", email);
    }
}