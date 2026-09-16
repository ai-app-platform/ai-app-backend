package com.aiplatform.domain.memory;

public record MemoryUpdateCommand(
    String title,
    String content,
    String summary,
    String metadata
) {}
