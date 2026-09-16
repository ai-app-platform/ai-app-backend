package com.aiplatform.domain.memory;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemoryRepository extends BaseRepository<Memory> {
    List<Memory> findByProjectId(UUID projectId);
    List<Memory> findByTaskId(UUID taskId);
    List<Memory> findByProjectIdAndMemoryType(UUID projectId, MemoryType memoryType);
    List<Memory> findByTaskIdAndMemoryType(UUID taskId, MemoryType memoryType);
    List<Memory> findByProjectIdAndPromoted(UUID projectId, boolean promoted);
}
