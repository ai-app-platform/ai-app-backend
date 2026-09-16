package com.aiplatform.domain.agent;

public record AgentUpdateCommand(
    String description,
    String capabilities,
    String defaultPromptRef,
    String modelConfiguration,
    String runtimeConfiguration,
    String executionPolicies
) {}
