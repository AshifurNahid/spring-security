package com.nahid.userservice.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service for token generation, validation, and parsing.
 *
 * Why we use HS256:
 * - Symmetric algorithm suitable for single-application scenarios
 * - Faster than RSA for token validation
 * - Simpler key management (single secret vs public/private key pair)
 *
 * When to consider RSA256:
 * - Microservices architecture where multiple services need to validate tokens
 * - When you need to distribute public keys for token validation
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.clock-skew:300000}") // 5 minutes default
    private long clockSkew;

    /**
     * Generates access token with user details and custom claims
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, accessTokenExpiration);
    }

    /**
     * Generates refresh token (simpler, longer-lived)
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshTokenExpiration);
    }

    /**
     * Core token building method
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic claim extraction method
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validates token against user details
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if token is expired (with clock skew tolerance)
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        // Add clock skew tolerance
        return expiration.before(new Date(System.currentTimeMillis() - clockSkew));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims with proper exception handling
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .setAllowedClockSkewSeconds(clockSkew / 1000) // Convert to seconds
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates signing key from secret
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates token format and signature (doesn't check expiration)
     */
    public boolean isTokenValidFormat(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid token format: {}", e.getMessage());
            return false;
        }
    }
}