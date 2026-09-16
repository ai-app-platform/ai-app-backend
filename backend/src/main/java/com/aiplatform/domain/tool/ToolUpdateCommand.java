package com.aiplatform.domain.tool;

public record ToolUpdateCommand(
    String description,
    String inputSchema,
    String outputSchema,
    PermissionLevel permissionLevel,
    String executionPolicy,
    String testingConfiguration
) {}
