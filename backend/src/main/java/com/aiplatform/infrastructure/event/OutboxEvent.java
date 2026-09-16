package com.aiplatform.infrastructure.event;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "occurred_on", nullable = false)
    private Instant occurredOn;

    @Column(name = "occurred_by")
    private String occurredBy;

    @Column(name = "processed", nullable = false)
    @Builder.Default
    private boolean processed = false;

    @Column(name = "processed_at")
    private Instant processedAt;

    public static OutboxEvent from(DomainEventWrapper event, String payloadJson) {
        return OutboxEvent.builder()
                .aggregateType(event.aggregateType())
                .aggregateId(event.aggregateId())
                .eventType(event.eventType())
                .payload(payloadJson)
                .occurredOn(event.occurredOn())
                .occurredBy(event.occurredBy())
                .build();
    }

    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = Instant.now();
    }
}
