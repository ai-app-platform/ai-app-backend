package com.aiplatform.interfaces.rest.dto;

public record ApiError(
    String code,
    String message,
    Object details
) {}
