package com.aiplatform.domain.rag;

import com.aiplatform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rag_documents", indexes = {
    @Index(name = "idx_rag_project", columnList = "project_id"),
    @Index(name = "idx_rag_source", columnList = "source_type"),
    @Index(name = "idx_rag_commit", columnList = "commit_sha")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RagDocument extends AggregateRoot {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "source_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RagSourceType sourceType;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "line_range_start")
    private Integer lineRangeStart;

    @Column(name = "line_range_end")
    private Integer lineRangeEnd;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "commit_sha")
    private String commitSha;

    @Column(name = "branch")
    private String branch;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "embedding_model_version")
    private String embeddingModelVersion;

    @Column(name = "parser_version")
    private String parserVersion;

    @Column(name = "index_version")
    private String indexVersion;

    @Override
    public String getAggregateType() {
        return "RAG_DOCUMENT";
    }
}
