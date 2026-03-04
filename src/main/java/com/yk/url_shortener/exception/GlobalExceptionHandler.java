package com.yk.url_shortener.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * Catches all unhandled exceptions and returns clean JSON error responses
 * instead of ugly stack traces or default Spring whitepage errors.
 *
 * Without this: client gets HTTP 500 with HTML or raw stack trace
 * With this:    client gets HTTP 4xx/5xx with structured JSON like:
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "URL cannot be blank",
 *   "path": "/api/shorten",
 *   "timestamp": "2026-03-04T12:00:00"
 * }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle @Valid / @Validated bean validation failures
     * Triggered when request body fails validation (e.g., blank URL, invalid format)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        Map<String, Object> body = buildErrorBody(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                fieldErrors.toString(),
                request.getRequestURI()
        );

        log.warn("Validation error on {}: {}", request.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handle rate limit exceeded (thrown by controller when RateLimiterService blocks a request)
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(
            RateLimitExceededException ex,
            HttpServletRequest request) {

        Map<String, Object> body = buildErrorBody(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("Rate limit exceeded for request to: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    /**
     * Handle any unexpected exception — last resort fallback
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        Map<String, Object> body = buildErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(body);
    }

    private Map<String, Object> buildErrorBody(int status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}

