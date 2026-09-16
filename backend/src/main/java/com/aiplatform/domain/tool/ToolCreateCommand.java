package com.aiplatform.domain.tool;

public record ToolCreateCommand(
    String name,
    String description,
    ToolType toolType,
    String capability,
    String inputSchema,
    String outputSchema,
    PermissionLevel permissionLevel,
    UUID connectorId,
    String executionPolicy,
    String testingConfiguration
) {}
