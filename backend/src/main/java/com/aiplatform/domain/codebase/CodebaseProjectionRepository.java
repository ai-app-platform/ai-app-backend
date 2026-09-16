package com.aiplatform.domain.codebase;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodebaseProjectionRepository extends BaseRepository<CodebaseProjection> {
    Optional<CodebaseProjection> findByProjectId(UUID projectId);
}
