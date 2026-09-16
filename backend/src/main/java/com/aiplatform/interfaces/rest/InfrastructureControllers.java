package com.aiplatform.interfaces.rest;

import com.aiplatform.domain.codebase.CodebaseIntelligenceService;
import com.aiplatform.domain.codebase.CodebaseProjection;
import com.aiplatform.domain.context.ContextEngineService;
import com.aiplatform.domain.workspace.Workspace;
import com.aiplatform.domain.workspace.WorkspaceService;
import com.aiplatform.interfaces.rest.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class InfrastructureControllers {

    // ============ Workspaces ============
    private final WorkspaceService workspaceService;

    @GetMapping("/workspaces")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getAllWorkspaces() {
        // In a real implementation, this would list all workspaces
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @GetMapping("/workspaces/{name}")
    public ResponseEntity<ApiResponse<WorkspaceDetailResponse>> getWorkspace(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.success(new WorkspaceDetailResponse(
                name, "active", "100MB", Instant.now().toString(), 100,
                "/tmp/workspaces/" + name, "success", Instant.now().toString(),
                new TestResults(10, 10, 0, 85.0),
                List.of(), Map.of("java", "21", "maven", "3.9", "node", "20")
        )));
    }

    @GetMapping("/workspaces/{name}/files")
    public ResponseEntity<ApiResponse<List<WorkspaceFileResponse>>> getWorkspaceFiles(
            @PathVariable String name,
            @RequestParam(defaultValue = "/") String path) {
        // In a real implementation, this would list files in the workspace
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @PostMapping("/workspaces/{name}/execute")
    public ResponseEntity<ApiResponse<ExecuteCommandResponse>> executeCommand(
            @PathVariable String name,
            @RequestBody ExecuteCommandRequest request) {
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", request.command());
            pb.directory(new File("/tmp/workspaces/" + name));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            return ResponseEntity.ok(ApiResponse.success(new ExecuteCommandResponse(output.toString(), exitCode)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success(new ExecuteCommandResponse(e.getMessage(), 1)));
        }
    }

    // ============ Codebase Intelligence ============
    private final CodebaseIntelligenceService codebaseIntelligenceService;

    @GetMapping("/projects/{projectId}/codebase/overview")
    public ResponseEntity<ApiResponse<CodebaseOverviewResponse>> getCodebaseOverview(@PathVariable UUID projectId) {
        Optional<CodebaseProjection> projection = codebaseIntelligenceService.getProjection(projectId);
        if (projection.isPresent()) {
            CodebaseProjection p = projection.get();
            return ResponseEntity.ok(ApiResponse.success(new CodebaseOverviewResponse(
                    100, 5000, Map.of("Java", 80, "XML", 15, "YAML", 5),
                    10, p.getGeneratedAt() != null ? p.getGeneratedAt().toString() : null,
                    "up-to-date"
            )));
        }
        return ResponseEntity.ok(ApiResponse.success(new CodebaseOverviewResponse(
                0, 0, Map.of(), 0, null, "outdated"
        )));
    }

    @GetMapping("/projects/{projectId}/codebase/modules")
    public ResponseEntity<ApiResponse<List<ModuleResponse>>> getCodebaseModules(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(List.of(
                new ModuleResponse("domain", 20, 15, List.of("shared")),
                new ModuleResponse("interfaces", 10, 8, List.of("domain")),
                new ModuleResponse("infrastructure", 15, 12, List.of("domain"))
        )));
    }

    @GetMapping("/projects/{projectId}/codebase/modules/{moduleName}")
    public ResponseEntity<ApiResponse<ModuleDetailResponse>> getModuleDetail(
            @PathVariable UUID projectId,
            @PathVariable String moduleName) {
        return ResponseEntity.ok(ApiResponse.success(new ModuleDetailResponse(
                moduleName, 20, 15, List.of("shared"), List.of("File1.java", "File2.java")
        )));
    }

    @PostMapping("/projects/{projectId}/codebase/search")
    public ResponseEntity<ApiResponse<List<CodebaseSearchResult>>> searchCodebase(
            @PathVariable UUID projectId,
            @RequestBody CodebaseSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @GetMapping("/projects/{projectId}/codebase/symbols/{symbolName}")
    public ResponseEntity<ApiResponse<SymbolDetailResponse>> getSymbolDetail(
            @PathVariable UUID projectId,
            @PathVariable String symbolName) {
        return ResponseEntity.ok(ApiResponse.success(new SymbolDetailResponse(
                symbolName, "class", "src/main/java/com/example/" + symbolName + ".java",
                1, List.of("method1", "method2"), List.of(), List.of()
        )));
    }

    @GetMapping("/projects/{projectId}/codebase/files")
    public ResponseEntity<ApiResponse<FileContentResponse>> getFileContent(
            @PathVariable UUID projectId,
            @RequestParam String path) {
        return ResponseEntity.ok(ApiResponse.success(new FileContentResponse(path, "// File content")));
    }

    // ============ Context Engine ============
    private final ContextEngineService contextEngineService;

    @GetMapping("/tasks/{taskId}/context")
    public ResponseEntity<ApiResponse<ContextResponse>> getTaskContext(@PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success(new ContextResponse(
                Instant.now().toString(),
                Map.of("platformKnowledge", 5, "projectKnowledge", 3, "instructions", 2,
                        "codeContext", 10, "memory", 4, "ragResults", 8, "toolDefinitions", 6),
                5000, 8000, "local-first",
                List.of(
                        new ContextSection("knowledge", 1500, "platform"),
                        new ContextSection("code", 2000, "workspace"),
                        new ContextSection("memory", 1000, "project"),
                        new ContextSection("rag", 500, "vector-store")
                )
        )));
    }

    // ============ Project Team ============
    @GetMapping("/projects/{projectId}/team")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> getProjectTeam(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(new TeamDetailResponse(
                UUID.randomUUID(), projectId, "Default Team", "1.0.0",
                List.of(), Instant.now().toString(), Instant.now().toString(), null
        )));
    }

    // ============ Git Pull Requests ============
    @GetMapping("/projects/{projectId}/git/pull-requests")
    public ResponseEntity<ApiResponse<List<PullRequestResponse>>> getPullRequests(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}
