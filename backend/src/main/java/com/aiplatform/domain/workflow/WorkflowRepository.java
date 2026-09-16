package com.aiplatform.domain.workflow;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends BaseRepository<Workflow> {
    List<Workflow> findByScope(WorkflowScope scope);
    List<Workflow> findByProjectId(UUID projectId);
    Optional<Workflow> findByNameAndScope(String name, WorkflowScope scope);
}
