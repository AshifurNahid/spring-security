package com.nahid.userservice.security;

import com.nahid.userservice.service.JwtService;
import com.nahid.userservice.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that processes Bearer tokens
 *
 * Why OncePerRequestFilter:
 * - Guarantees filter is executed only once per request
 * - Handles async and error dispatch scenarios properly
 * - Spring Boot recommendation for custom authentication filters
 *
 * How it works:
 * 1. Extracts JWT from Authorization header
 * 2. Validates token format and signature
 * 3. Loads user details and sets authentication context
 * 4. Continues filter chain with authenticated context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip authentication for auth endpoints
        String requestPath = request.getServletPath();
        if (requestPath.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // Check if Authorization header is present and valid
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7); // Remove "Bearer " prefix
            final String userEmail = jwtService.extractUsername(jwt);

            // If user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details
                UserDetails userDetails = userService.loadUserByUsername(userEmail);

                // Validate token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("User {} authenticated successfully", userEmail);
                } else {
                    log.debug("Invalid JWT token for user: {}", userEmail);
                }
            }
        } catch (JwtException e) {
            log.debug("JWT processing error: {}", e.getMessage());
            // Don't set authentication - let it continue as unauthenticated
        } catch (Exception e) {
            log.error("Error processing JWT: {}", e.getMessage());
            // Don't set authentication - let it continue as unauthenticated
        }

        filterChain.doFilter(request, response);
    }
}