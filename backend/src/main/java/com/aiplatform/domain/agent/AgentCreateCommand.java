package com.aiplatform.domain.agent;

public record AgentCreateCommand(
    String name,
    String description,
    AgentType agentType,
    String capabilities,
    String defaultPromptRef,
    String modelConfiguration,
    String runtimeConfiguration,
    String executionPolicies
) {}
