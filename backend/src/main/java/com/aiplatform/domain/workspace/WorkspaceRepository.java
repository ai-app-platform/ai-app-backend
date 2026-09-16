package com.aiplatform.domain.workspace;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends BaseRepository<Workspace> {
    Optional<Workspace> findByProjectId(UUID projectId);
}
