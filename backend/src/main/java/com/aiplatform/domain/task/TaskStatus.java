package com.aiplatform.domain.task;

public enum TaskStatus {
    CREATED,
    PLANNING,
    AGENT_SELECTION,
    EXECUTING,
    VALIDATING,
    REVIEWING,
    COMPLETING,
    COMPLETED,
    FAILED,
    CANCELLED
}
