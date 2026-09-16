package com.aiplatform.shared.exception;

public class BusinessRuleViolationException extends DomainException {
    public BusinessRuleViolationException(String rule, String message) {
        super("BUSINESS_RULE_VIOLATION:" + rule, message);
    }
}
