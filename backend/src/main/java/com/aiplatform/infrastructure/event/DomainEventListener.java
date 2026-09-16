package com.aiplatform.infrastructure.event;

import com.aiplatform.shared.domain.DomainEventWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Domain Event Listener - processes events asynchronously.
 * In production, this would persist to an outbox table for reliable delivery.
 */
@Slf4j
@Component
public class DomainEventListener {

    @Async
    @EventListener
    public void handleDomainEvent(DomainEventWrapper event) {
        log.info("Received domain event: {} for {} [{}] at {}",
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.occurredOn());

        // In production: persist to outbox table, then publish to message broker
    }
}
