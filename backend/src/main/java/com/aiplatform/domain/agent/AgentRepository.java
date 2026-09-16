package com.aiplatform.domain.agent;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRepository extends BaseRepository<Agent> {
    Optional<Agent> findByName(String name);
    boolean existsByName(String name);
    List<Agent> findByAgentType(AgentType agentType);
    Page<Agent> findByStatus(com.aiplatform.shared.domain.EntityStatus status, Pageable pageable);
}
