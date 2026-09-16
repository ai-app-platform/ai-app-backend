package com.aiplatform.domain.task;

import com.aiplatform.domain.git.GitManagementService;
import com.aiplatform.domain.memory.ShortTermMemoryService;
import com.aiplatform.domain.workspace.WorkspaceService;
import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Task Orchestration Service - Manages complete task lifecycle:
 * 1. Create task
 * 2. Create task branch from main
 * 3. Execute agent team
 * 4. Apply changes
 * 5. Commit and push
 * 6. Create pull request
 * 7. Complete task
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskOrchestrationService {

    private final TaskService taskService;
    private final GitManagementService gitManagementService;
    private final WorkspaceService workspaceService;
    private final ShortTermMemoryService shortTermMemoryService;

    @Transactional
    public Task createAndInitializeTask(TaskCreateCommand command) {
        // 1. Create task
        Task task = taskService.create(command);
        log.info("Created task: {} for project: {}", task.getId(), command.projectId());

        // 2. Get workspace path
        String workspacePath = workspaceService.findByProjectOrThrow(command.projectId()).getBasePath();

        // 3. Create task branch
        String taskBranch = "task/" + task.getId().toString().substring(0, 8);
        gitManagementService.createTaskBranch(command.projectId(), taskBranch);
        task.setTaskBranch(taskBranch);

        // 4. Initialize runtime memory
        shortTermMemoryService.writeCurrentTask(workspacePath, task.getId(), command.description());
        shortTermMemoryService.writeCurrentPlan(workspacePath, "Task initialized, waiting for execution");

        // 5. Update task status
        task.transitionTo(TaskStatus.PLANNING);
        task = taskService.update(task.getId(), new TaskUpdateCommand(
                task.getTitle(),
                task.getDescription(),
                task.getExecutionMode(),
                task.getPriority(),
                task.getMetadata()
        ));

        log.info("Task {} initialized with branch: {}", task.getId(), taskBranch);
        return task;
    }

    @Transactional
    public Task executeTask(UUID taskId) {
        Task task = taskService.findByIdOrThrow(taskId);

        if (task.getTaskStatus() != TaskStatus.PLANNING) {
            throw new BusinessRuleViolationException("INVALID_TASK_STATE",
                    "Task must be in PLANNING state to execute");
        }

        // Transition to AGENT_SELECTION
        task.transitionTo(TaskStatus.AGENT_SELECTION);

        // In a real implementation, this would:
        // 1. Use Planner to create execution plan
        // 2. Select agents from project team
        // 3. Create execution graph
        // 4. Execute workflow with LangGraph

        task.setSelectedAgents("[\"architect\", \"developer\", \"reviewer\"]");
        task.setPlan("{\"steps\":[\"analyze\",\"implement\",\"review\",\"test\"]}");

        // Transition to EXECUTING
        task.transitionTo(TaskStatus.EXECUTING);

        log.info("Task {} is now executing", taskId);
        return task;
    }

    @Transactional
    public Task completeTaskExecution(UUID taskId, String result) {
        Task task = taskService.findByIdOrThrow(taskId);

        if (task.getTaskStatus() != TaskStatus.EXECUTING) {
            throw new BusinessRuleViolationException("INVALID_TASK_STATE",
                    "Task must be in EXECUTING state to complete");
        }

        // Update result
        task.setResult(result);

        // Transition to VALIDATING
        task.transitionTo(TaskStatus.VALIDATING);

        // In a real implementation, this would:
        // 1. Run build
        // 2. Run tests
        // 3. Run security scan
        // 4. Check code quality

        // Transition to REVIEWING
        task.transitionTo(TaskStatus.REVIEWING);

        // In a real implementation, this would:
        // 1. Code review by reviewer agent
        // 2. Approval if needed

        // Transition to COMPLETING
        task.transitionTo(TaskStatus.COMPLETING);

        // Commit and push changes
        String workspacePath = workspaceService.findByProjectOrThrow(task.getProjectId()).getBasePath();
        String commitMessage = String.format("Task %s: %s", task.getId(), task.getTitle());
        gitManagementService.commitAndPush(task.getProjectId(), task.getTaskBranch(), commitMessage);

        // Create pull request
        String prUrl = gitManagementService.createPullRequest(
                task.getProjectId(),
                task.getTaskBranch(),
                "main",
                task.getTitle()
        );

        // Update runtime memory
        shortTermMemoryService.writeTaskSummary(workspacePath, task.getId(),
                "Task completed successfully. PR created: " + prUrl);

        // Transition to COMPLETED
        task.transitionTo(TaskStatus.COMPLETED);

        log.info("Task {} completed. PR: {}", taskId, prUrl);
        return task;
    }

    @Transactional
    public Task failTask(UUID taskId, String error) {
        Task task = taskService.findByIdOrThrow(taskId);

        task.setResult(String.format("{\"error\":\"%s\"}", error));
        task.transitionTo(TaskStatus.FAILED);

        String workspacePath = workspaceService.findByProjectOrThrow(task.getProjectId()).getBasePath();
        shortTermMemoryService.writeTaskSummary(workspacePath, task.getId(),
                "Task failed: " + error);

        log.error("Task {} failed: {}", taskId, error);
        return task;
    }

    @Transactional
    public Task cancelTask(UUID taskId) {
        Task task = taskService.findByIdOrThrow(taskId);
        task.transitionTo(TaskStatus.CANCELLED);

        String workspacePath = workspaceService.findByProjectOrThrow(task.getProjectId()).getBasePath();
        shortTermMemoryService.writeTaskSummary(workspacePath, task.getId(), "Task cancelled");

        log.info("Task {} cancelled", taskId);
        return task;
    }

    public Optional<Task> findById(UUID id) {
        return taskService.findById(id);
    }

    public Task findByIdOrThrow(UUID id) {
        return taskService.findByIdOrThrow(id);
    }

    public List<Task> findAll(int page, int size) {
        return taskService.findAll(page, size);
    }

    public List<Task> findByProject(UUID projectId) {
        return taskService.findProjectTasks(projectId);
    }

    @Transactional
    public Task updateStatus(UUID taskId, String status) {
        Task task = findByIdOrThrow(taskId);
        TaskStatus newStatus = TaskStatus.valueOf(status.toUpperCase());
        task.transitionTo(newStatus);
        return task;
    }
}
