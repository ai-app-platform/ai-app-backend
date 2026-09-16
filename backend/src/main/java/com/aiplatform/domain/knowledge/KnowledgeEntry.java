package com.aiplatform.domain.knowledge;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import com.aiplatform.shared.domain.VersionInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "knowledge_entries", indexes = {
    @Index(name = "idx_knowledge_scope", columnList = "scope"),
    @Index(name = "idx_knowledge_project", columnList = "project_id"),
    @Index(name = "idx_knowledge_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KnowledgeEntry extends AggregateRoot {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    @Builder.Default
    private KnowledgeScope scope = KnowledgeScope.PLATFORM;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "workflow_id")
    private UUID workflowId;

    @Column(name = "source_ref")
    private String sourceRef;

    @Column(name = "source_commit_sha")
    private String sourceCommitSha;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

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
        return "KNOWLEDGE_ENTRY";
    }

    public boolean isPlatformKnowledge() {
        return this.scope == KnowledgeScope.PLATFORM;
    }

    public boolean isProjectKnowledge() {
        return this.scope == KnowledgeScope.PROJECT;
    }

    public void publishNewVersion() {
        this.versionInfo = this.versionInfo.increment();
    }
}
