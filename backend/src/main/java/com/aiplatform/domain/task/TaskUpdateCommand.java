package com.aiplatform.domain.task;

public record TaskUpdateCommand(
    String title,
    String description,
    ExecutionMode executionMode,
    Integer priority,
    String metadata
) {}
