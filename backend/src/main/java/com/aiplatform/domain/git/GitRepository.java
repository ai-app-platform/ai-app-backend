package com.aiplatform.domain.git;

import com.aiplatform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "git_repositories", indexes = {
    @Index(name = "idx_git_project", columnList = "project_id"),
    @Index(name = "idx_git_connector", columnList = "connector_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GitRepository extends AggregateRoot {

    @Column(name = "project_id", nullable = false, unique = true)
    private UUID projectId;

    @Column(name = "connector_id")
    private UUID connectorId;

    @Column(name = "remote_url", nullable = false)
    private String remoteUrl;

    @Column(name = "local_path")
    private String localPath;

    @Column(name = "default_branch")
    @Builder.Default
    private String defaultBranch = "main";

    @Column(name = "current_branch")
    private String currentBranch;

    @Column(name = "last_commit_sha")
    private String lastCommitSha;

    @Column(name = "last_fetch_at")
    private java.time.Instant lastFetchAt;

    @Column(name = "clone_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CloneStatus cloneStatus = CloneStatus.NOT_CLONED;

    @Override
    public String getAggregateType() {
        return "GIT_REPOSITORY";
    }

    public boolean isCloned() {
        return this.cloneStatus == CloneStatus.CLONED;
    }

    public void markAsCloned(String localPath) {
        this.cloneStatus = CloneStatus.CLONED;
        this.localPath = localPath;
    }

    public void updateLastFetch() {
        this.lastFetchAt = java.time.Instant.now();
    }
}
