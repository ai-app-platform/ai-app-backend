package com.aiplatform.domain.skill;

public record SkillUpdateCommand(
    String description,
    String category,
    ExpertiseLevel expertiseLevel,
    String configuration
) {}
