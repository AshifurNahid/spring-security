package com.nahid.userservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler using RFC 7807 Problem Details
 *
 * Why RFC 7807:
 * - Standardized error response format
 * - Machine-readable error details
 * - Consistent error handling across the API
 * - Better client-side error handling
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        log.debug("Validation error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed"
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://example.com/problems/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        // Add field-specific errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        problemDetail.setProperty("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handles custom authentication exceptions
     */
    @ExceptionHandler(com.nahid.userservice.exception.AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request
    ) {
        log.debug("Authentication error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setType(URI.create("https://example.com/problems/authentication-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * Handles Spring Security authentication exceptions
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ProblemDetail> handleSpringAuthenticationException(
            Exception ex,
            WebRequest request
    ) {
        log.debug("Spring Security authentication error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setType(URI.create("https://example.com/problems/authentication-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    /**
     * Handles access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request
    ) {
        log.debug("Access denied: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Access denied"
        );
        problemDetail.setTitle("Access Denied");
        problemDetail.setType(URI.create("https://example.com/problems/access-denied"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    /**
     * Handles resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        log.debug("Resource not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create("https://example.com/problems/resource-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handles all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://example.com/problems/internal-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
}