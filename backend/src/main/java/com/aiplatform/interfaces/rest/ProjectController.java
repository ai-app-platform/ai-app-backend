package com.aiplatform.interfaces.rest;

import com.aiplatform.domain.project.Project;
import com.aiplatform.domain.project.*;
import com.aiplatform.interfaces.rest.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects() {
        List<Project> projects = projectService.findAll(0, 100);
        List<ProjectResponse> response = projects.stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProject(@PathVariable UUID id) {
        Project project = projectService.findByIdOrThrow(id);
        ProjectDetailResponse response = mapToDetailResponse(project);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@RequestBody CreateProjectRequest request) {
        ProjectCreateCommand command = new ProjectCreateCommand(
                request.name(),
                request.description(),
                request.gitUrl(),
                null, // defaultBranch
                request.connectorId() != null ? request.connectorId().toString() : null,
                null  // configuration
        );

        Project project = projectService.create(command);
        ProjectResponse response = mapToResponse(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @RequestBody UpdateProjectRequest request) {
        ProjectUpdateCommand command = new ProjectUpdateCommand(
                request.name(),
                request.description(),
                request.gitUrl(),
                null,
                null
        );

        Project project = projectService.update(id, command);
        ProjectResponse response = mapToResponse(project);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().name().toLowerCase(),
                "java", // Default
                project.getRepositoryUrl(),
                project.getUpdatedAt() != null ? project.getUpdatedAt().toString() : null,
                0, // agents count
                0, // tasks count
                project.getWorkspacePath()
        );
    }

    private ProjectDetailResponse mapToDetailResponse(Project project) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus().name().toLowerCase(),
                "java",
                project.getRepositoryUrl(),
                project.getCreatedAt() != null ? project.getCreatedAt().toString() : null,
                project.getUpdatedAt() != null ? project.getUpdatedAt().toString() : null,
                project.getConnectorId() != null ? project.getConnectorId().toString() : null,
                project.getWorkspacePath(),
                null, // teamId
                null, // defaultWorkflow
                new ProjectStats(0, 0, 0, 0, 0),
                List.of(),
                java.util.Map.of()
        );
    }
}
