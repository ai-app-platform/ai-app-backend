package com.aiplatform.domain.project;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_projects_name", columnList = "name"),
    @Index(name = "idx_projects_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project extends AggregateRoot {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "repository_url", nullable = false)
    private String repositoryUrl;

    @Column(name = "default_branch")
    @Builder.Default
    private String defaultBranch = "main";

    @Column(name = "workspace_path")
    private String workspacePath;

    @Column(name = "connector_id")
    private java.util.UUID connectorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "configuration", columnDefinition = "jsonb")
    private String configuration;

    @Override
    public String getAggregateType() {
        return "PROJECT";
    }

    public boolean isActive() {
        return this.status == EntityStatus.ACTIVE;
    }

    public void activate() {
        this.status = EntityStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = EntityStatus.INACTIVE;
    }

    public void archive() {
        this.status = EntityStatus.ARCHIVED;
    }
}
