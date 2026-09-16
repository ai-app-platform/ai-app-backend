package com.aiplatform.domain.role;

public record RoleCreateCommand(
    String name,
    String description,
    String responsibility,
    String constraints,
    String promptPolicyRef,
    String skillPolicyRef,
    String toolPolicyRef
) {}
