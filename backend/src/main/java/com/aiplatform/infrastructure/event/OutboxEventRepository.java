package com.aiplatform.infrastructure.event;

import com.aiplatform.shared.domain.DomainEventWrapper;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByProcessedFalseOrderByOccurredOnAsc();
    List<OutboxEvent> findByAggregateId(UUID aggregateId);
}
