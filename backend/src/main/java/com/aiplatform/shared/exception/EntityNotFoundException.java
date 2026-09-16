package com.aiplatform.shared.exception;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entityType, Object identifier) {
        super("ENTITY_NOT_FOUND", String.format("%s not found with identifier: %s", entityType, identifier));
    }
}
