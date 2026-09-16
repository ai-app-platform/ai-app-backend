package com.aiplatform.domain.memory;

import java.util.UUID;

public record MemoryCreateCommand(
    UUID projectId,
    UUID taskId,
    MemoryType memoryType,
    String title,
    String content,
    String filePath,
    String metadata
) {}
