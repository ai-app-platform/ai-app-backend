package com.aiplatform.shared.exception;

public class ConcurrencyException extends DomainException {
    public ConcurrencyException(String entityType, Object identifier) {
        super("CONCURRENCY_CONFLICT",
            String.format("Concurrent modification detected for %s: %s", entityType, identifier));
    }
}
