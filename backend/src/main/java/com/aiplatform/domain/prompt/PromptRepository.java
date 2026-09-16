package com.aiplatform.domain.prompt;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptRepository extends BaseRepository<Prompt> {
    Optional<Prompt> findByNameAndScope(String name, PromptScope scope);
    List<Prompt> findByScope(PromptScope scope);
    List<Prompt> findByProjectId(UUID projectId);
    List<Prompt> findByScopeAndProjectId(PromptScope scope, UUID projectId);
}
