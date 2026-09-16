package com.aiplatform.infrastructure.event;

import com.aiplatform.shared.domain.DomainEventWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event Publisher using Spring's ApplicationEventPublisher.
 * Implements the Outbox Pattern concept - events are published after transaction commit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(String aggregateType, UUID aggregateId, String eventType, Object payload, String occurredBy) {
        DomainEventWrapper event = new DomainEventWrapper(
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                eventType,
                payload,
                Instant.now(),
                occurredBy
        );

        log.debug("Publishing domain event: {} for {} [{}]", eventType, aggregateType, aggregateId);
        applicationEventPublisher.publishEvent(event);
    }

    public void publishEntityCreated(String aggregateType, UUID aggregateId, Object entity) {
        publish(aggregateType, aggregateId, aggregateType + "_CREATED", entity, "system");
    }

    public void publishEntityUpdated(String aggregateType, UUID aggregateId, Object entity) {
        publish(aggregateType, aggregateId, aggregateType + "_UPDATED", entity, "system");
    }

    public void publishEntityDeleted(String aggregateType, UUID aggregateId) {
        publish(aggregateType, aggregateId, aggregateType + "_DELETED", null, "system");
    }
}
