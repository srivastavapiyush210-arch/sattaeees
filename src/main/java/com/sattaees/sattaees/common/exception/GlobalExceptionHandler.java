package com.sattaees.sattaees.common.exception;

import com.sattaees.sattaees.common.dto.ErrorResponse;
import com.sattaees.sattaees.common.filter.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler providing uniform error responses,
 * trace ID propagation, and secure error messages.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} | URI: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource conflict: {} | URI: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
        log.warn("Invalid business operation: {} | URI: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized access attempt: {} | URI: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed (bad credentials) on URI: {}", request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid email or password.", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for URI: {} | Reason: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "Access denied: You lack necessary permissions for this resource.", request, null);
    }

    @ExceptionHandler({OptimisticLockConflictException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockConflict(Exception ex, HttpServletRequest request) {
        log.warn("Optimistic concurrency conflict on URI: {} | Details: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Concurrency Conflict",
                "The resource was modified by another concurrent transaction. Please refresh and retry.",
                request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String message = String.join(", ", errors);
        log.warn("Validation failed ({} errors) on URI: {}", errors.size(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", message, request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed HTTP request body on URI: {}", request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed Request", "Malformed JSON request body.", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled internal server error on URI: {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected internal error occurred. Please contact system support with the trace ID.",
                request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message,
                                                        HttpServletRequest request, List<String> validationErrors) {
        String traceId = MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY);
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .traceId(traceId)
                .validationErrors(validationErrors)
                .build();
        return new ResponseEntity<>(body, status);
    }
}
