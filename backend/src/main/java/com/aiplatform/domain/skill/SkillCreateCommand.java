package com.aiplatform.domain.skill;

public record SkillCreateCommand(
    String name,
    String description,
    String category,
    ExpertiseLevel expertiseLevel,
    String configuration
) {}
