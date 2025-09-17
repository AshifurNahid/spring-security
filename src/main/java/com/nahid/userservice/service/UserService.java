package com.nahid.userservice.service;

import com.nahid.userservice.dto.LogoutResponse;
import com.nahid.userservice.dto.UserResponse;
import com.nahid.userservice.entity.RefreshToken;
import com.nahid.userservice.entity.User;
import com.nahid.userservice.exception.AuthenticationException;
import com.nahid.userservice.repository.RefreshTokenRepository;
import com.nahid.userservice.repository.UserRepository;
import com.nahid.userservice.util.contant.ExceptionMessageConstant;
import com.nahid.userservice.util.contant.AppConstant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.nahid.userservice.util.contant.ExceptionMessageConstant.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    String.format(ExceptionMessageConstant.ENTITY_NOT_FOUND_BY_FIELD,
                        AppConstant.USER, "email", username)));
    }

    public UserResponse getMe() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public LogoutResponse logout(String authHeader) {
        log.info("Attempting logout with authorization header");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }

        String refreshToken = authHeader.substring(7);
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException(INVALID_REFRESH_TOKEN));

        if (tokenEntity.isRevoked()) {
            throw new AuthenticationException(REFRESH_TOKEN_ALREADY_REVOKED);
        }

        if (tokenEntity.isExpired()) {
            throw new AuthenticationException(REFRESH_TOKEN_EXPIRED);
        }


        if (!tokenEntity.getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new RuntimeException(REFRESH_TOKEN_OWNERSHIP_MISMATCH);
        }

        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        return LogoutResponse.builder()
                .message("Logged out successfully")
                .success(true)
                .build();
    }

}