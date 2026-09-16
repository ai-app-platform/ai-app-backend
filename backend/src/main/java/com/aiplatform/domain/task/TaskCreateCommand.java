package com.aiplatform.domain.task;

import java.util.UUID;

public record TaskCreateCommand(
    UUID projectId,
    String title,
    String description,
    TaskType taskType,
    UUID workflowId,
    ExecutionMode executionMode,
    Integer priority,
    String metadata
) {}
