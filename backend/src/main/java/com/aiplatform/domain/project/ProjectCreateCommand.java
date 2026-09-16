package com.aiplatform.domain.project;

public record ProjectCreateCommand(
    String name,
    String description,
    String repositoryUrl,
    String defaultBranch,
    String configuration
) {}
