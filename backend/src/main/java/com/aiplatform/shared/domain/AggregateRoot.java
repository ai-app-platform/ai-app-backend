package com.aiplatform.shared.domain;

public abstract class AggregateRoot extends BaseEntity {

    public abstract String getAggregateType();

    public boolean isNew() {
        return getId() == null;
    }
}
