package com.aiplatform.domain.knowledge;

public record KnowledgeUpdateCommand(
    String title,
    String content,
    String category,
    Integer priority,
    String tags
) {}
