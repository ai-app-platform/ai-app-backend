package com.aiplatform.domain.task;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends BaseRepository<Task> {
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByProjectIdAndTaskStatus(UUID projectId, TaskStatus taskStatus);
    List<Task> findByTaskStatus(TaskStatus taskStatus);
}
