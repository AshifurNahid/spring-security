package com.nahid.userservice.service;

import com.nahid.userservice.dto.*;
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

    @Value( "${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Transactional
    public RegisterResponse register(RegisterRequest request) throws AuthenticationException {
        log.info("Attempting to register user with email: {}", request.getEmail());

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

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole())
                .build();
    }


    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.getEmail(),
                        request.getPassword());
        Authentication authentication = authenticationManager.authenticate(
                token
        );

        User user = (User) authentication.getPrincipal();
        log.info("User authenticated successfully: {}", user.getEmail());

        refreshTokenRepository.revokeAllByUser(user);

        return generateTokenAndResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Attempting token refresh");

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            log.warn("Attempted use of revoked refresh token");
            throw new AuthenticationException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            log.warn("Attempted use of expired refresh token");
            refreshTokenRepository.delete(refreshToken); // Delete instead of keeping
            throw new AuthenticationException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);
        log.info("Token refreshed successfully for user: {}", user.getEmail());
        return generateTokenAndResponse(user);
    }


    private AuthResponse generateTokenAndResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn( accessTokenExpiration) // 15 minutes in seconds
                .build();
    }

    @Transactional
    public void revokeAllUserTokens(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.revokeAllByUser(user);
        log.info("All refresh tokens revoked for user: {}", email);
    }

    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        log.info("Attempting logout with refresh token");

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            log.warn("Attempted logout with already revoked refresh token");
            throw new AuthenticationException("Refresh token has already been revoked");
        }

        refreshTokenRepository.delete(refreshToken);

        log.info("User logged out successfully: {}", refreshToken.getUser().getEmail());

        return LogoutResponse.builder()
                .message("Logged out successfully")
                .success(true)
                .build();
    }
}