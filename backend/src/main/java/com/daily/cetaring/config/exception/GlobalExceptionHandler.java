package com.daily.cetaring.config.exception;

import com.daily.cetaring.features.auth.service.OtpRateLimitException;
import com.daily.cetaring.shared.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OtpRateLimitException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleOtpRateLimitException(
            OtpRateLimitException ex, WebRequest request) {
        log.warn("OTP rate limited: {} retryAfter={}s", ex.getMessage(), ex.getRetryAfterSeconds());
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.TOO_MANY_REQUESTS.value())
                .message(ex.getMessage())
                .data(Map.of("error", ex.getMessage(), "retryAfterSeconds", ex.getRetryAfterSeconds()))
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .data(Map.of("error", ex.getMessage()))
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.warn("Bad credentials attempt");
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid email/username or password")
                .data(Map.of("error", "Invalid credentials"))
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.FORBIDDEN.value())
                .message("Access denied")
                .data(Map.of("error", "You don't have permission to perform this action."))
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {
        int statusCode = ex.getStatusCode().value();
        String reason = ex.getReason() == null ? "Request failed" : ex.getReason();
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(statusCode)
                .message(reason)
                .data(Map.of("error", reason))
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        String errorId = newErrorId();
        log.warn("Data integrity violation errorId={} path={} cause={}", errorId, path(request), safeCause(ex));
        String message = "This request conflicts with existing data. Please refresh and try again.";
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.CONFLICT.value())
                .message(message)
                .data(Map.of("error", message, "errorId", errorId))
                .path(path(request))
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, Object> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> {
                    String text = error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage();
                    return !text.isEmpty() && Character.isUpperCase(text.charAt(0))
                            ? text
                            : readableField(error.getField()) + ": " + text;
                })
                .orElse("Some details are missing or invalid. Please review and try again.");
        log.warn("Validation failed path={} fields={}", path(request), errors.keySet());
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .data(errors)
                .path(path(request))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Unreadable request path={} cause={}", path(request), ex.getMostSpecificCause().getClass().getSimpleName());
        String message = "Some details are in an unexpected format. Please update the app and try again.";
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .data(Map.of("error", message))
                .path(path(request))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        return serverError(ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleGenericException(
            Exception ex, WebRequest request) {
        return serverError(ex, request);
    }

    /** Never echoes exception text (may contain SQL or PII); the errorId correlates the client report with logs. */
    private ResponseEntity<ApiResponse<Map<String, String>>> serverError(Exception ex, WebRequest request) {
        String errorId = newErrorId();
        log.error("Unexpected error errorId={} path={} cause={}", errorId, path(request), safeCause(ex), ex);
        String message = "Something went wrong on our server. Please try again. (Ref: " + errorId + ")";
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(message)
                .data(Map.of("error", "An unexpected error occurred", "errorId", errorId))
                .path(path(request))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private static String newErrorId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String path(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    static String safeCause(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        String message = root.getMessage() == null ? "" : root.getMessage();
        // Mask long digit runs (mobile numbers, OTPs) before logging.
        message = message.replaceAll("\\d{6,}", "******");
        if (message.length() > 300) message = message.substring(0, 300) + "...";
        return root.getClass().getName() + ": " + message;
    }

    private static String readableField(String field) {
        String leaf = field.replaceAll("\\[\\d+]", "");
        int dot = leaf.lastIndexOf('.');
        leaf = dot >= 0 ? leaf.substring(dot + 1) : leaf;
        String spaced = leaf.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
        return spaced.isEmpty() ? "Request" : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
