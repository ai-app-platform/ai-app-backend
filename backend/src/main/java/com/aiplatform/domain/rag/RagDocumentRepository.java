package com.aiplatform.domain.rag;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RagDocumentRepository extends BaseRepository<RagDocument> {
    List<RagDocument> findByProjectId(UUID projectId);
    List<RagDocument> findByProjectIdAndSourceType(UUID projectId, RagSourceType sourceType);
    List<RagDocument> findByCommitSha(String commitSha);
    Optional<RagDocument> findByProjectIdAndFilePath(UUID projectId, String filePath);
    void deleteByProjectIdAndCommitSha(UUID projectId, String commitSha);
}
