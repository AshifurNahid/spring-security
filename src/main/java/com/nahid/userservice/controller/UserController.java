package com.nahid.userservice.controller;

import com.nahid.userservice.dto.UserResponse;
import com.nahid.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User Controller handling user-related operations
 *
 * Security:
 * - All endpoints require authentication
 * - Uses @PreAuthorize for method-level security
 * - Principal extraction from Security Context
 */
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile

     * What: Returns authenticated user's profile information
     * Why: Frontend needs user details for UI personalization
     * How: Extracts user from Security Context, returns safe user data
     * When: Called after successful authentication to get user details
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Fetching profile for user: {}", email);

        UserResponse userResponse = userService.getCurrentUser(email);

        return ResponseEntity.ok(userResponse);
    }
}