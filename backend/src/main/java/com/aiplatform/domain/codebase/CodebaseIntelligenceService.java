package com.aiplatform.domain.codebase;

import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodebaseIntelligenceService {

    private final CodebaseProjectionRepository codebaseProjectionRepository;

    @Transactional
    public CodebaseProjection performFullAnalysis(UUID projectId, String commitSha) {
        CodebaseProjection projection = codebaseProjectionRepository.findByProjectId(projectId)
                .orElse(CodebaseProjection.builder().projectId(projectId).build());

        // In a real implementation, this would:
        // 1. Parse all source files using AST
        // 2. Build code graph (classes, methods, dependencies)
        // 3. Generate overview, architecture, modules, dependencies, conventions
        // 4. Store the code graph as JSON

        projection.markAsGenerated(commitSha, true);
        projection.setParserVersion("1.0.0");
        projection.setGeneratorVersion("1.0.0");

        return codebaseProjectionRepository.save(projection);
    }

    @Transactional
    public CodebaseProjection performIncrementalAnalysis(UUID projectId, String commitSha,
                                                          java.util.List<String> changedFiles) {
        CodebaseProjection projection = codebaseProjectionRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("CodebaseProjection", projectId));

        // In a real implementation, this would:
        // 1. Parse only changed files
        // 2. Update affected symbols in code graph
        // 3. Update vector index for changed files
        // 4. Regenerate affected projections

        projection.markAsGenerated(commitSha, false);
        return codebaseProjectionRepository.save(projection);
    }

    public Optional<CodebaseProjection> getProjection(UUID projectId) {
        return codebaseProjectionRepository.findByProjectId(projectId);
    }

    public CodebaseProjection getProjectionOrThrow(UUID projectId) {
        return codebaseProjectionRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("CodebaseProjection", projectId));
    }

    public boolean isStale(UUID projectId, String currentCommitSha) {
        return codebaseProjectionRepository.findByProjectId(projectId)
                .map(p -> p.isStale(currentCommitSha))
                .orElse(true);
    }

    public String getModuleContext(UUID projectId, String moduleName) {
        CodebaseProjection projection = getProjectionOrThrow(projectId);
        // In a real implementation, this would extract module-specific context from the code graph
        return projection.getModules();
    }

    public String getFileContext(UUID projectId, String filePath) {
        CodebaseProjection projection = getProjectionOrThrow(projectId);
        // In a real implementation, this would extract file-specific context
        return projection.getOverview();
    }

    public String getSymbolContext(UUID projectId, String symbol) {
        CodebaseProjection projection = getProjectionOrThrow(projectId);
        // In a real implementation, this would traverse the code graph for the symbol
        return projection.getCodeGraph();
    }
}
