package com.aiplatform.domain.workflow;

public record WorkflowUpdateCommand(
    String description,
    String definition,
    Boolean planningEnabled,
    String agentSelectionStrategy,
    Boolean allowParallel,
    Boolean validationRequired,
    Boolean approvalRequired,
    String steps,
    String retryPolicy,
    String failureHandling
) {}
