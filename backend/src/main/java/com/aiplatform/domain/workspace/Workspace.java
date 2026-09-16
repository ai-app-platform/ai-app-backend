package com.aiplatform.domain.workspace;

import com.aiplatform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "workspaces", indexes = {
    @Index(name = "idx_workspaces_project", columnList = "project_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Workspace extends AggregateRoot {

    @Column(name = "project_id", nullable = false, unique = true)
    private UUID projectId;

    @Column(name = "base_path", nullable = false)
    private String basePath;

    @Column(name = "current_task_branch")
    private String currentTaskBranch;

    @Column(name = "workspace_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WorkspaceStatus workspaceStatus = WorkspaceStatus.INITIALIZED;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Override
    public String getAggregateType() {
        return "WORKSPACE";
    }

    public boolean isReady() {
        return this.workspaceStatus == WorkspaceStatus.READY;
    }

    public void markAsReady() {
        this.workspaceStatus = WorkspaceStatus.READY;
    }

    public String getTaskWorkspacePath(String taskBranch) {
        return this.basePath + "/" + taskBranch;
    }

    public String getAiDirectoryPath() {
        return this.basePath + "/.ai";
    }
}
