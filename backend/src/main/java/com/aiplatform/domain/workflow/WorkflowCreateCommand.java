package com.aiplatform.domain.workflow;

import java.util.UUID;

public record WorkflowCreateCommand(
    String name,
    String description,
    WorkflowScope scope,
    UUID projectId,
    String definition,
    boolean planningEnabled,
    String agentSelectionStrategy,
    boolean allowParallel,
    boolean validationRequired,
    boolean approvalRequired,
    String steps,
    String retryPolicy,
    String failureHandling
) {}
