package com.aiplatform.domain.project;

public record ProjectUpdateCommand(
    String name,
    String description,
    String repositoryUrl,
    String defaultBranch,
    String configuration
) {}
