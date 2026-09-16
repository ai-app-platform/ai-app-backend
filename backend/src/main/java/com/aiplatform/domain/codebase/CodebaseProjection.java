package com.aiplatform.domain.codebase;

import com.aiplatform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "codebase_projections", indexes = {
    @Index(name = "idx_codebase_project", columnList = "project_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CodebaseProjection extends AggregateRoot {

    @Column(name = "project_id", nullable = false, unique = true)
    private UUID projectId;

    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;

    @Column(name = "architecture", columnDefinition = "TEXT")
    private String architecture;

    @Column(name = "modules", columnDefinition = "TEXT")
    private String modules;

    @Column(name = "dependencies", columnDefinition = "TEXT")
    private String dependencies;

    @Column(name = "conventions", columnDefinition = "TEXT")
    private String conventions;

    @Column(name = "code_graph", columnDefinition = "jsonb")
    private String codeGraph;

    @Column(name = "source_commit_sha")
    private String sourceCommitSha;

    @Column(name = "parser_version")
    private String parserVersion;

    @Column(name = "generator_version")
    private String generatorVersion;

    @Column(name = "generated_at")
    private java.time.Instant generatedAt;

    @Column(name = "full_index", nullable = false)
    @Builder.Default
    private boolean fullIndex = false;

    @Override
    public String getAggregateType() {
        return "CODEBASE_PROJECTION";
    }

    public boolean isStale(String currentCommitSha) {
        return !currentCommitSha.equals(this.sourceCommitSha);
    }

    public void markAsGenerated(String commitSha, boolean fullIndex) {
        this.sourceCommitSha = commitSha;
        this.generatedAt = java.time.Instant.now();
        this.fullIndex = fullIndex;
    }
}
