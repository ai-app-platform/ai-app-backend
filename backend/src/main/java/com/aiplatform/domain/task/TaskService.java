package com.aiplatform.domain.task;

import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import com.aiplatform.shared.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService implements CrudService<Task, TaskCreateCommand, TaskUpdateCommand> {

    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public Task create(TaskCreateCommand command) {
        validateCommand(command);

        Task task = Task.builder()
                .projectId(command.projectId())
                .title(command.title())
                .description(command.description())
                .taskType(command.taskType() != null ? command.taskType() : TaskType.SOFTWARE_DEVELOPMENT)
                .workflowId(command.workflowId())
                .executionMode(command.executionMode() != null ? command.executionMode() : ExecutionMode.AUTOMATIC)
                .priority(command.priority() != null ? command.priority() : 0)
                .metadata(command.metadata())
                .taskBranch("task/" + UUID.randomUUID().toString().substring(0, 8))
                .build();

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task update(UUID id, TaskUpdateCommand command) {
        Task task = findByIdOrThrow(id);

        if (task.getTaskStatus() != TaskStatus.CREATED) {
            throw new BusinessRuleViolationException("TASK_IMMUTABLE_AFTER_START",
                    "Task can only be modified in CREATED status");
        }

        if (command.title() != null) task.setTitle(command.title());
        if (command.description() != null) task.setDescription(command.description());
        if (command.executionMode() != null) task.setExecutionMode(command.executionMode());
        if (command.priority() != null) task.setPriority(command.priority());
        if (command.metadata() != null) task.setMetadata(command.metadata());

        return taskRepository.save(task);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return taskRepository.findById(id);
    }

    @Override
    public List<Task> findAll(int page, int size) {
        return taskRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Task task = findByIdOrThrow(id);
        task.transitionTo(TaskStatus.CANCELLED);
        taskRepository.save(task);
    }

    public Task findByIdOrThrow(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task", id));
    }

    public List<Task> findProjectTasks(UUID projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public List<Task> findActiveTasks(UUID projectId) {
        return taskRepository.findByProjectIdAndTaskStatus(projectId, TaskStatus.EXECUTING);
    }

    @Transactional
    public Task transitionStatus(UUID taskId, TaskStatus newStatus) {
        Task task = findByIdOrThrow(taskId);
        task.transitionTo(newStatus);
        return taskRepository.save(task);
    }

    @Transactional
    public Task setPlan(UUID taskId, String plan) {
        Task task = findByIdOrThrow(taskId);
        task.setPlan(plan);
        return taskRepository.save(task);
    }

    @Transactional
    public Task setSelectedAgents(UUID taskId, String selectedAgents) {
        Task task = findByIdOrThrow(taskId);
        task.setSelectedAgents(selectedAgents);
        return taskRepository.save(task);
    }

    @Transactional
    public Task setResult(UUID taskId, String result) {
        Task task = findByIdOrThrow(taskId);
        task.setResult(result);
        return taskRepository.save(task);
    }

    private void validateCommand(TaskCreateCommand command) {
        if (command.projectId() == null) {
            throw new BusinessRuleViolationException("TASK_PROJECT_REQUIRED", "Project ID is required");
        }
        if (command.title() == null || command.title().isBlank()) {
            throw new BusinessRuleViolationException("TASK_TITLE_REQUIRED", "Task title is required");
        }
    }
}
