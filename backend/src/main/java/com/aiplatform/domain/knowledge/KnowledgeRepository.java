package com.aiplatform.domain.knowledge;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeRepository extends BaseRepository<KnowledgeEntry> {
    List<KnowledgeEntry> findByScope(KnowledgeScope scope);
    List<KnowledgeEntry> findByProjectId(UUID projectId);
    List<KnowledgeEntry> findByCategory(String category);
    List<KnowledgeEntry> findByScopeAndProjectId(KnowledgeScope scope, UUID projectId);
}
