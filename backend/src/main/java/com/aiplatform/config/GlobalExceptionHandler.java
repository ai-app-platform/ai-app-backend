package com.aiplatform.config;

import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.ConcurrencyException;
import com.aiplatform.shared.exception.DomainException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleViolationException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errorBody(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(ConcurrencyException.class)
    public ResponseEntity<Map<String, Object>> handleConcurrency(ConcurrencyException ex) {
        log.error("Concurrency conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(OptimisticLockingFailureException ex) {
        log.error("Optimistic locking failure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody("CONCURRENCY_CONFLICT", "Entity was modified by another transaction"));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomain(DomainException ex) {
        log.error("Domain exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody("INVALID_STATE", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private Map<String, Object> errorBody(String code, String message) {
        return Map.of(
                "errorCode", code,
                "message", message,
                "timestamp", Instant.now().toString()
        );
    }
}
