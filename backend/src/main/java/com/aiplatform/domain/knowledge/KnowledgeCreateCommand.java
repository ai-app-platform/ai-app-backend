package com.aiplatform.domain.knowledge;

import java.util.UUID;

public record KnowledgeCreateCommand(
    String title,
    String content,
    String category,
    KnowledgeScope scope,
    UUID projectId,
    UUID workflowId,
    String sourceRef,
    String sourceCommitSha,
    String filePath,
    Integer priority,
    String tags
) {}
