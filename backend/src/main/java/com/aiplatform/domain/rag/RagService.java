package com.aiplatform.domain.rag;

import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RagService {

    private final RagDocumentRepository ragDocumentRepository;

    @Transactional
    public RagDocument indexDocument(UUID projectId, RagSourceType sourceType,
                                      String filePath, String content, String commitSha,
                                      String branch, String contentHash) {
        validateIndexInput(projectId, content);

        RagDocument document = RagDocument.builder()
                .projectId(projectId)
                .sourceType(sourceType)
                .filePath(filePath)
                .content(content)
                .contentHash(contentHash)
                .commitSha(commitSha)
                .branch(branch)
                .build();

        return ragDocumentRepository.save(document);
    }

    @Transactional
    public RagDocument indexCodeSymbol(UUID projectId, String filePath, String symbol,
                                        String content, String commitSha,
                                        Integer lineStart, Integer lineEnd, String contentHash) {
        RagDocument document = RagDocument.builder()
                .projectId(projectId)
                .sourceType(RagSourceType.SOURCE_CODE)
                .filePath(filePath)
                .symbol(symbol)
                .lineRangeStart(lineStart)
                .lineRangeEnd(lineEnd)
                .content(content)
                .contentHash(contentHash)
                .commitSha(commitSha)
                .build();

        return ragDocumentRepository.save(document);
    }

    public Optional<RagDocument> findById(UUID id) {
        return ragDocumentRepository.findById(id);
    }

    public List<RagDocument> findByProject(UUID projectId) {
        return ragDocumentRepository.findByProjectId(projectId);
    }

    public List<RagDocument> findByProjectAndType(UUID projectId, RagSourceType sourceType) {
        return ragDocumentRepository.findByProjectIdAndSourceType(projectId, sourceType);
    }

    @Transactional
    public void reindexForCommit(UUID projectId, String previousCommit, String newCommit) {
        // Incremental indexing: only re-index changed files
        // In a real implementation, this would:
        // 1. Get git diff between previousCommit and newCommit
        // 2. Identify changed files
        // 3. Re-parse affected symbols
        // 4. Update vector index
        // 5. Update graph
    }

    @Transactional
    public void fullReindex(UUID projectId) {
        ragDocumentRepository.findByProjectId(projectId).forEach(doc -> {
            // Re-index each document
        });
    }

    private void validateIndexInput(UUID projectId, String content) {
        if (projectId == null) {
            throw new BusinessRuleViolationException("RAG_PROJECT_REQUIRED", "Project ID is required for indexing");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessRuleViolationException("RAG_CONTENT_REQUIRED", "Content is required for indexing");
        }
    }
}
