package com.aiplatform.interfaces.rest;

import com.aiplatform.domain.task.*;
import com.aiplatform.interfaces.rest.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskOrchestrationService taskOrchestrationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks(
            @RequestParam(required = false) UUID projectId) {
        List<Task> tasks;
        if (projectId != null) {
            tasks = taskOrchestrationService.findByProject(projectId);
        } else {
            tasks = taskOrchestrationService.findAll(0, 100);
        }
        List<TaskResponse> response = tasks.stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDetailResponse>> getTask(@PathVariable UUID id) {
        Task task = taskOrchestrationService.findByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success(mapToDetailResponse(task)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@RequestBody CreateTaskRequest request) {
        TaskCreateCommand command = new TaskCreateCommand(
                request.projectId(),
                request.title(),
                request.description(),
                TaskType.SOFTWARE_DEVELOPMENT,
                request.workflowId(),
                ExecutionMode.AUTOMATIC,
                mapPriority(request.priority()),
                null
        );
        Task task = taskOrchestrationService.createAndInitializeTask(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapToResponse(task)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable UUID id,
            @RequestBody UpdateTaskStatusRequest request) {
        Task task = taskOrchestrationService.updateStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(task)));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiResponse<TaskResponse>> executeTask(@PathVariable UUID id) {
        Task task = taskOrchestrationService.executeTask(id);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(task)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(
            @PathVariable UUID id,
            @RequestBody(required = false) String result) {
        Task task = taskOrchestrationService.completeTaskExecution(id, result);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(task)));
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getProjectId(),
                task.getTaskStatus().name().toLowerCase(),
                mapPriorityToString(task.getPriority()),
                List.of(),
                task.getWorkflowId() != null ? task.getWorkflowId().toString() : null,
                task.getCreatedAt() != null ? task.getCreatedAt().toString() : null,
                null
        );
    }

    private TaskDetailResponse mapToDetailResponse(Task task) {
        return new TaskDetailResponse(
                task.getId(),
                task.getTitle(),
                task.getProjectId(),
                task.getTaskStatus().name().toLowerCase(),
                mapPriorityToString(task.getPriority()),
                List.of(),
                task.getWorkflowId() != null ? task.getWorkflowId().toString() : null,
                task.getCreatedAt() != null ? task.getCreatedAt().toString() : null,
                null,
                task.getDescription(),
                List.of(),
                List.of(),
                new GitInfo(task.getTaskBranch(), 0, 0),
                new ValidationInfo(null, null, null, null)
        );
    }

    private int mapPriority(String priority) {
        return switch (priority != null ? priority.toLowerCase() : "medium") {
            case "high" -> 2;
            case "low" -> 0;
            default -> 1;
        };
    }

    private String mapPriorityToString(int priority) {
        return switch (priority) {
            case 2 -> "high";
            case 0 -> "low";
            default -> "medium";
        };
    }
}
