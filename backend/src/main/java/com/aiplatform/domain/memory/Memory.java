package com.aiplatform.domain.memory;

import com.aiplatform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "memories", indexes = {
    @Index(name = "idx_memories_project", columnList = "project_id"),
    @Index(name = "idx_memories_task", columnList = "task_id"),
    @Index(name = "idx_memories_type", columnList = "memory_type")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Memory extends AggregateRoot {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "task_id")
    private UUID taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "memory_type", nullable = false)
    private MemoryType memoryType;

    @Column(name = "title")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "promoted", nullable = false)
    @Builder.Default
    private boolean promoted = false;

    @Column(name = "summary")
    private String summary;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Override
    public String getAggregateType() {
        return "MEMORY";
    }

    public boolean isActiveMemory() {
        return this.memoryType == MemoryType.ACTIVE;
    }

    public boolean isLongTermMemory() {
        return this.memoryType == MemoryType.LONG_TERM;
    }

    public void promoteToLongTerm(String summary) {
        this.memoryType = MemoryType.LONG_TERM;
        this.promoted = true;
        this.summary = summary;
    }
}
