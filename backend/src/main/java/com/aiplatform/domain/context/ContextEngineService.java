package com.aiplatform.domain.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Context Engine is responsible for collecting and assembling context for Agent execution.
 * It does NOT own any source data - it only collects and assembles.
 * 
 * Retrieval Strategy (Local-first, Source-of-Truth-first):
 * 1. Direct Task Context
 * 2. Project Knowledge / Instructions
 * 3. Current Project Codebase Intelligence
 * 4. Workspace / Local Code
 * 5. Relevant Memory
 * 6. RAG / Deep Retrieval
 * 7. External Connector Data
 */
@Service
@RequiredArgsConstructor
public class ContextEngineService {

    private final com.aiplatform.domain.knowledge.KnowledgeService knowledgeService;
    private final com.aiplatform.domain.memory.MemoryService memoryService;
    private final com.aiplatform.domain.codebase.CodebaseIntelligenceService codebaseIntelligenceService;

    /**
     * Assembles the complete runtime context for an agent execution.
     */
    public AgentContext assembleContext(ContextRequest request) {
        AgentContext.AgentContextBuilder contextBuilder = AgentContext.builder()
                .taskId(request.taskId())
                .projectId(request.projectId())
                .agentId(request.agentId());

        // Level 1: Resolve Knowledge (Platform → Project → Workflow)
        var knowledge = knowledgeService.resolveKnowledge(
                request.projectId(), request.workflowId());
        contextBuilder.resolvedKnowledge(knowledge);

        // Level 2: Load Project Memory
        var activeMemories = memoryService.findActiveMemories(request.projectId());
        contextBuilder.activeMemories(activeMemories);

        // Level 3: Load Task-specific Memory
        if (request.taskId() != null) {
            var taskMemories = memoryService.findTaskMemories(request.taskId());
            contextBuilder.taskMemories(taskMemories);
        }

        // Level 4: Load Codebase Intelligence
        if (request.projectId() != null) {
            codebaseIntelligenceService.getProjection(request.projectId())
                    .ifPresent(contextBuilder::codebaseProjection);
        }

        // Level 5: Add direct context items
        if (request.additionalContext() != null) {
            contextBuilder.additionalContext(request.additionalContext());
        }

        return contextBuilder.build();
    }

    /**
     * Retrieves local-first context for a specific module.
     */
    public String getModuleContext(UUID projectId, String moduleName) {
        return codebaseIntelligenceService.getModuleContext(projectId, moduleName);
    }

    /**
     * Retrieves local-first context for a specific file.
     */
    public String getFileContext(UUID projectId, String filePath) {
        return codebaseIntelligenceService.getFileContext(projectId, filePath);
    }

    /**
     * Retrieves local-first context for a specific symbol.
     */
    public String getSymbolContext(UUID projectId, String symbol) {
        return codebaseIntelligenceService.getSymbolContext(projectId, symbol);
    }

    public record ContextRequest(
            UUID taskId,
            UUID projectId,
            UUID agentId,
            UUID workflowId,
            String role,
            List<String> requiredSkills,
            Map<String, String> additionalContext
    ) {}

    @lombok.Builder
    @lombok.Getter
    public static class AgentContext {
        private UUID taskId;
        private UUID projectId;
        private UUID agentId;
        private List<com.aiplatform.domain.knowledge.KnowledgeEntry> resolvedKnowledge;
        private List<com.aiplatform.domain.memory.Memory> activeMemories;
        private List<com.aiplatform.domain.memory.Memory> taskMemories;
        private com.aiplatform.domain.codebase.CodebaseProjection codebaseProjection;
        private Map<String, String> additionalContext;
    }
}
