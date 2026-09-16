package com.aiplatform.domain.role;

public record RoleUpdateCommand(
    String description,
    String responsibility,
    String constraints,
    String promptPolicyRef,
    String skillPolicyRef,
    String toolPolicyRef
) {}
