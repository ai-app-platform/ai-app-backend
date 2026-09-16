package com.aiplatform.domain.workflow;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import com.aiplatform.shared.domain.VersionInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "workflows", indexes = {
    @Index(name = "idx_workflows_name", columnList = "name"),
    @Index(name = "idx_workflows_scope", columnList = "scope"),
    @Index(name = "idx_workflows_project", columnList = "project_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Workflow extends AggregateRoot {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    @Builder.Default
    private WorkflowScope scope = WorkflowScope.PLATFORM;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "definition", nullable = false, columnDefinition = "TEXT")
    private String definition;

    @Column(name = "planning_enabled")
    @Builder.Default
    private boolean planningEnabled = true;

    @Column(name = "agent_selection_strategy")
    @Builder.Default
    private String agentSelectionStrategy = "dynamic";

    @Column(name = "allow_parallel")
    @Builder.Default
    private boolean allowParallel = true;

    @Column(name = "validation_required")
    @Builder.Default
    private boolean validationRequired = true;

    @Column(name = "approval_required")
    @Builder.Default
    private boolean approvalRequired = false;

    @Column(name = "steps", columnDefinition = "jsonb")
    private String steps;

    @Column(name = "retry_policy", columnDefinition = "jsonb")
    private String retryPolicy;

    @Column(name = "failure_handling", columnDefinition = "jsonb")
    private String failureHandling;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "semanticVersion", column = @Column(name = "version_semantic")),
        @AttributeOverride(name = "latest", column = @Column(name = "version_latest")),
        @AttributeOverride(name = "versionSequence", column = @Column(name = "version_sequence"))
    })
    private VersionInfo versionInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Override
    public String getAggregateType() {
        return "WORKFLOW";
    }

    public boolean isPlatformWorkflow() {
        return this.scope == WorkflowScope.PLATFORM;
    }

    public void publishNewVersion() {
        this.versionInfo = this.versionInfo.increment();
    }
}
