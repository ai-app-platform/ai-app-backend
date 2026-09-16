package com.aiplatform.domain.prompt;

public record PromptUpdateCommand(
    String description,
    String template,
    String variables,
    String modelConfiguration,
    String compositionRules
) {}
