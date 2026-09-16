package com.aiplatform.domain.prompt;

import java.util.UUID;

public record PromptCreateCommand(
    String name,
    String description,
    PromptScope scope,
    UUID projectId,
    String template,
    String variables,
    String modelConfiguration,
    String compositionRules
) {}
