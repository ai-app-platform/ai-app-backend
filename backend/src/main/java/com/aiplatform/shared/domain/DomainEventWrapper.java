package com.aiplatform.shared.domain;

import java.time.Instant;
import java.util.UUID;

public record DomainEventWrapper(
    UUID eventId,
    String aggregateType,
    UUID aggregateId,
    String eventType,
    Object payload,
    Instant occurredOn,
    String occurredBy
) implements DomainEvent {

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }
}
