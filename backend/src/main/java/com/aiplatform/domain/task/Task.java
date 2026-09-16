package com.aiplatform.domain.task;

import com.aiplatform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_project", columnList = "project_id"),
    @Index(name = "idx_tasks_status", columnList = "task_status"),
    @Index(name = "idx_tasks_workflow", columnList = "workflow_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Task extends AggregateRoot {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskType taskType = TaskType.SOFTWARE_DEVELOPMENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false)
    @Builder.Default
    private TaskStatus taskStatus = TaskStatus.CREATED;

    @Column(name = "workflow_id")
    private UUID workflowId;

    @Column(name = "plan", columnDefinition = "jsonb")
    private String plan;

    @Column(name = "selected_agents", columnDefinition = "jsonb")
    private String selectedAgents;

    @Column(name = "execution_graph", columnDefinition = "jsonb")
    private String executionGraph;

    @Column(name = "execution_mode")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExecutionMode executionMode = ExecutionMode.AUTOMATIC;

    @Column(name = "task_branch")
    private String taskBranch;

    @Column(name = "base_commit_sha")
    private String baseCommitSha;

    @Column(name = "result", columnDefinition = "jsonb")
    private String result;

    @Column(name = "execution_trace", columnDefinition = "jsonb")
    private String executionTrace;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Override
    public String getAggregateType() {
        return "TASK";
    }

    public void transitionTo(TaskStatus newStatus) {
        validateTransition(newStatus);
        this.taskStatus = newStatus;
    }

    private void validateTransition(TaskStatus newStatus) {
        switch (this.taskStatus) {
            case CREATED:
                if (newStatus != TaskStatus.PLANNING && newStatus != TaskStatus.CANCELLED) {
                    throw invalidTransition(newStatus);
                }
                break;
            case PLANNING:
                if (newStatus != TaskStatus.AGENT_SELECTION && newStatus != TaskStatus.CANCELLED) {
                    throw invalidTransition(newStatus);
                }
                break;
            case AGENT_SELECTION:
                if (newStatus != TaskStatus.EXECUTING && newStatus != TaskStatus.CANCELLED) {
                    throw invalidTransition(newStatus);
                }
                break;
            case EXECUTING:
                if (newStatus != TaskStatus.VALIDATING && newStatus != TaskStatus.FAILED && newStatus != TaskStatus.CANCELLED) {
                    throw invalidTransition(newStatus);
                }
                break;
            case VALIDATING:
                if (newStatus != TaskStatus.REVIEWING && newStatus != TaskStatus.EXECUTING && newStatus != TaskStatus.FAILED) {
                    throw invalidTransition(newStatus);
                }
                break;
            case REVIEWING:
                if (newStatus != TaskStatus.COMPLETING && newStatus != TaskStatus.EXECUTING && newStatus != TaskStatus.FAILED) {
                    throw invalidTransition(newStatus);
                }
                break;
            case COMPLETING:
                if (newStatus != TaskStatus.COMPLETED && newStatus != TaskStatus.FAILED) {
                    throw invalidTransition(newStatus);
                }
                break;
            case COMPLETED, FAILED, CANCELLED:
                throw invalidTransition(newStatus);
        }
    }

    private IllegalStateException invalidTransition(TaskStatus newStatus) {
        return new IllegalStateException(
            String.format("Invalid status transition from %s to %s", this.taskStatus, newStatus));
    }
}
