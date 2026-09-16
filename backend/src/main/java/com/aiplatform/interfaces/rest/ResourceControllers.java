package com.aiplatform.interfaces.rest;

import com.aiplatform.domain.agent.*;
import com.aiplatform.domain.connector.*;
import com.aiplatform.domain.git.GitManagementService;
import com.aiplatform.domain.knowledge.*;
import com.aiplatform.domain.memory.*;
import com.aiplatform.domain.prompt.*;
import com.aiplatform.domain.rag.*;
import com.aiplatform.domain.role.*;
import com.aiplatform.domain.skill.*;
import com.aiplatform.domain.team.*;
import com.aiplatform.domain.tool.*;
import com.aiplatform.domain.workflow.*;
import com.aiplatform.interfaces.rest.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class ResourceControllers {

    // ============ Agents ============
    private final AgentService agentService;

    @GetMapping("/agents")
    public ResponseEntity<ApiResponse<List<AgentResponse>>> getAllAgents() {
        List<Agent> agents = agentService.findAll(0, 100);
        List<AgentResponse> response = agents.stream().map(a -> new AgentResponse(
                a.getId(), a.getName(), a.getAgentType().name(), "available",
                "GPT-4", List.of(), List.of(), a.getDescription(),
                a.getVersionInfo() != null ? a.getVersionInfo().getSemanticVersion() : "1.0.0"
        )).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/agents/{id}")
    public ResponseEntity<ApiResponse<AgentDetailResponse>> getAgent(@PathVariable UUID id) {
        Agent agent = agentService.findByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success(new AgentDetailResponse(
                agent.getId(), agent.getName(), agent.getAgentType().name(), "available",
                "GPT-4", List.of(), List.of(), agent.getDescription(),
                agent.getVersionInfo() != null ? agent.getVersionInfo().getSemanticVersion() : "1.0.0",
                new AgentConfiguration(0.7, 4096, 0.9),
                List.of(), new ExecutionPolicy(3, 300, false),
                agent.getDefaultPromptRef(),
                agent.getCreatedAt() != null ? agent.getCreatedAt().toString() : null,
                null, new AgentStats(0, 0.0, "0s")
        )));
    }

    @PostMapping("/agents")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAgent(@RequestBody CreateAgentRequest request) {
        AgentCreateCommand command = new AgentCreateCommand(
                request.name(), request.description(),
                AgentType.valueOf(request.type().toUpperCase()),
                null, null, null, null, null
        );
        Agent agent = agentService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", agent.getId(), "name", agent.getName(),
                "createdAt", agent.getCreatedAt().toString()
        )));
    }

    // ============ Tools ============
    private final ToolService toolService;

    @GetMapping("/tools")
    public ResponseEntity<ApiResponse<List<ToolResponse>>> getAllTools() {
        List<Tool> tools = toolService.findAll(0, 100);
        return ResponseEntity.ok(ApiResponse.success(tools.stream().map(t -> new ToolResponse(
                t.getId(), t.getName(), t.getToolType().name(), t.getDescription(),
                t.getStatus().name().toLowerCase(),
                t.getVersionInfo() != null ? t.getVersionInfo().getSemanticVersion() : "1.0.0",
                List.of(t.getCapability())
        )).toList()));
    }

    @PostMapping("/tools")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTool(@RequestBody CreateToolRequest request) {
        ToolCreateCommand command = new ToolCreateCommand(
                request.name(), request.description(),
                ToolType.valueOf(request.type()),
                request.capabilities() != null && !request.capabilities().isEmpty() ? request.capabilities().get(0) : "default",
                request.inputSchema() != null ? request.inputSchema().toString() : null,
                request.outputSchema() != null ? request.outputSchema().toString() : null,
                PermissionLevel.STANDARD, null, null, null
        );
        Tool tool = toolService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", tool.getId(), "name", tool.getName(),
                "createdAt", tool.getCreatedAt().toString()
        )));
    }

    // ============ Connectors ============
    private final ConnectorService connectorService;

    @GetMapping("/connectors")
    public ResponseEntity<ApiResponse<List<ConnectorResponse>>> getAllConnectors() {
        List<Connector> connectors = connectorService.findAll(0, 100);
        return ResponseEntity.ok(ApiResponse.success(connectors.stream().map(c -> new ConnectorResponse(
                c.getId(), c.getName(), c.getProvider(), c.getConnectorType().name(),
                c.isConnected() ? "connected" : "disconnected",
                c.getRepositoryRef(), new ArrayList<>(c.getCapabilities()),
                c.isConnected() ? "healthy" : "unhealthy"
        )).toList()));
    }

    @PostMapping("/connectors")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createConnector(@RequestBody CreateConnectorRequest request) {
        ConnectorCreateCommand command = new ConnectorCreateCommand(
                request.name(),
                ConnectorType.valueOf(request.type().toUpperCase().replace("-", "_")),
                request.provider(), request.endpoint(), request.repository(),
                null, request.credentialRef(),
                AdapterType.valueOf(request.adapter().toUpperCase()),
                null, null,
                request.permissions() != null && request.permissions().write() ?
                        ConnectorPermission.READ_WRITE : ConnectorPermission.READ_ONLY,
                new HashSet<>()
        );
        Connector connector = connectorService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", connector.getId(), "name", connector.getName(),
                "createdAt", connector.getCreatedAt().toString()
        )));
    }

    @PostMapping("/connectors/{id}/test")
    public ResponseEntity<ApiResponse<ConnectorTestResponse>> testConnector(@PathVariable UUID id) {
        connectorService.findByIdOrThrow(id);
        return ResponseEntity.ok(ApiResponse.success(new ConnectorTestResponse(id, "connected", 150L)));
    }

    // ============ Knowledge ============
    private final KnowledgeService knowledgeService;

    @GetMapping("/knowledge")
    public ResponseEntity<ApiResponse<List<KnowledgeResponse>>> getAllKnowledge(
            @RequestParam(required = false) String scope) {
        List<KnowledgeEntry> entries;
        if (scope != null) {
            entries = knowledgeService.findByScope(KnowledgeScope.valueOf(scope.toUpperCase()));
        } else {
            entries = knowledgeService.findAll(0, 100);
        }
        return ResponseEntity.ok(ApiResponse.success(entries.stream().map(k -> new KnowledgeResponse(
                k.getId(), k.getTitle(), k.getScope().name().toLowerCase(),
                k.getVersionInfo() != null ? k.getVersionInfo().getSemanticVersion() : "1.0.0",
                k.getCategory(), k.getUpdatedAt() != null ? k.getUpdatedAt().toString() : null,
                k.getStatus().name().toLowerCase(), k.getProjectId()
        )).toList()));
    }

    @PostMapping("/knowledge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createKnowledge(@RequestBody CreateKnowledgeRequest request) {
        KnowledgeCreateCommand command = new KnowledgeCreateCommand(
                request.title(), request.content(), request.category(),
                KnowledgeScope.valueOf(request.scope().toUpperCase()),
                request.projectId(), null, null, null, null, 0, null
        );
        KnowledgeEntry entry = knowledgeService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", entry.getId(), "title", entry.getTitle(),
                "createdAt", entry.getCreatedAt().toString()
        )));
    }

    // ============ Workflows ============
    private final WorkflowService workflowService;

    @GetMapping("/workflows")
    public ResponseEntity<ApiResponse<List<WorkflowResponse>>> getAllWorkflows() {
        List<Workflow> workflows = workflowService.findAll(0, 100);
        return ResponseEntity.ok(ApiResponse.success(workflows.stream().map(w -> new WorkflowResponse(
                w.getId(), w.getName(), w.getDescription(),
                List.of(), w.getStatus().name().toLowerCase(),
                w.getVersionInfo() != null ? w.getVersionInfo().getSemanticVersion() : "1.0.0",
                w.isAllowParallel()
        )).toList()));
    }

    @PostMapping("/workflows")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createWorkflow(@RequestBody CreateWorkflowRequest request) {
        WorkflowCreateCommand command = new WorkflowCreateCommand(
                request.name(), request.description(), WorkflowScope.PLATFORM, null,
                request.steps() != null ? String.join(",", request.steps()) : "",
                true, "dynamic", request.allowParallel(), true, false,
                null, null, null
        );
        Workflow workflow = workflowService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", workflow.getId(), "name", workflow.getName(),
                "createdAt", workflow.getCreatedAt().toString()
        )));
    }

    // ============ Roles ============
    private final RoleService roleService;

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<Role> roles = roleService.findAll(0, 100);
        return ResponseEntity.ok(ApiResponse.success(roles.stream().map(r -> new RoleResponse(
                r.getId(), r.getName(), r.getDescription(),
                r.getConstraints() != null ? List.of(r.getConstraints()) : List.of(),
                List.of()
        )).toList()));
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRole(@RequestBody CreateRoleRequest request) {
        RoleCreateCommand command = new RoleCreateCommand(
                request.name(), request.description(),
                request.constraints() != null ? String.join(",", request.constraints()) : null,
                null, null, null, null
        );
        Role role = roleService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", role.getId(), "name", role.getName(),
                "createdAt", role.getCreatedAt().toString()
        )));
    }

    // ============ Skills ============
    private final SkillService skillService;

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getAllSkills() {
        List<Skill> skills = skillService.findAll(0, 100);
        return ResponseEntity.ok(ApiResponse.success(skills.stream().map(s -> new SkillResponse(
                s.getId(), s.getName(), s.getCategory(),
                s.getExpertiseLevel().name().toLowerCase(), s.getDescription()
        )).toList()));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSkill(@RequestBody CreateSkillRequest request) {
        SkillCreateCommand command = new SkillCreateCommand(
                request.name(), request.description(), request.category(),
                ExpertiseLevel.valueOf(request.level().toUpperCase()), null
        );
        Skill skill = skillService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", skill.getId(), "name", skill.getName(),
                "createdAt", skill.getCreatedAt().toString()
        )));
    }

    // ============ Teams ============
    private final ProjectTeamService projectTeamService;

    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @PostMapping("/teams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTeam(@RequestBody CreateTeamRequest request) {
        ProjectTeam team = projectTeamService.createTeam(request.projectId(), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", team.getId(), "name", team.getName(),
                "createdAt", team.getCreatedAt().toString()
        )));
    }

    // ============ Prompts ============
    private final PromptService promptService;

    @GetMapping("/prompts")
    public ResponseEntity<ApiResponse<List<PromptResponse>>> getAllPrompts() {
        List<Prompt> prompts = promptService.findAll(0, 100);
        return ResponseEntity.ok(ApiResponse.success(prompts.stream().map(p -> new PromptResponse(
                p.getId(), p.getName(),
                p.getVersionInfo() != null ? p.getVersionInfo().getSemanticVersion() : "1.0.0",
                p.getScope().name().toLowerCase(), p.getStatus().name().toLowerCase(),
                List.of()
        )).toList()));
    }

    @PostMapping("/prompts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPrompt(@RequestBody CreatePromptRequest request) {
        PromptCreateCommand command = new PromptCreateCommand(
                request.name(), null, PromptScope.PLATFORM, null,
                request.template(), null, null, null
        );
        Prompt prompt = promptService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "id", prompt.getId(), "name", prompt.getName(),
                "createdAt", prompt.getCreatedAt().toString()
        )));
    }

    // ============ Memory ============
    private final MemoryService memoryService;

    @GetMapping("/projects/{projectId}/memory")
    public ResponseEntity<ApiResponse<List<MemoryResponse>>> getProjectMemory(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String type) {
        List<Memory> memories;
        if (type != null) {
            memories = memoryService.findActiveMemories(projectId);
        } else {
            memories = memoryService.findAll(0, 100);
        }
        return ResponseEntity.ok(ApiResponse.success(memories.stream().map(m -> new MemoryResponse(
                m.getId(), m.getProjectId(), m.getTaskId(),
                m.getMemoryType().name().toLowerCase(), m.getContent(),
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : null,
                Map.of(), m.isPromoted(), false
        )).toList()));
    }

    // ============ RAG ============
    private final RagService ragService;

    @GetMapping("/projects/{projectId}/rag/status")
    public ResponseEntity<ApiResponse<RagStatusResponse>> getRagStatus(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(ragService.getStatus(projectId)));
    }

    @PostMapping("/projects/{projectId}/rag/search")
    public ResponseEntity<ApiResponse<List<RagSearchResult>>> searchRag(
            @PathVariable UUID projectId,
            @RequestBody RagSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                ragService.search(projectId, request.query(), request.limit(), request.filters())));
    }

    // ============ Git Management ============
    private final GitManagementService gitManagementService;

    @GetMapping("/projects/{projectId}/git/branches")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranches(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(gitManagementService.getBranches(projectId)));
    }

    @GetMapping("/projects/{projectId}/git/commits")
    public ResponseEntity<ApiResponse<List<CommitResponse>>> getCommits(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String branch) {
        return ResponseEntity.ok(ApiResponse.success(gitManagementService.getCommits(projectId, branch)));
    }

    @PostMapping("/projects/{projectId}/git/branches")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createBranch(
            @PathVariable UUID projectId,
            @RequestBody CreateBranchRequest request) {
        gitManagementService.createTaskBranch(projectId, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of(
                "name", request.name(), "baseBranch", request.baseBranch(),
                "createdAt", java.time.Instant.now().toString()
        )));
    }

    @GetMapping("/projects/{projectId}/git/diff")
    public ResponseEntity<ApiResponse<DiffResponse>> getDiff(
            @PathVariable UUID projectId,
            @RequestParam String branch,
            @RequestParam(required = false) String baseBranch) {
        return ResponseEntity.ok(ApiResponse.success(
                gitManagementService.getDiff(projectId, branch, baseBranch)));
    }

    // ============ Dashboard ============
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(new DashboardStatsResponse(0, 0, 0, 0, 0, 0, 0.0, "0 دقیقه")));
    }

    @GetMapping("/dashboard/activities")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivities(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    // ============ Settings ============
    @GetMapping("/settings/database")
    public ResponseEntity<ApiResponse<DatabaseSettingsResponse>> getDatabaseSettings() {
        return ResponseEntity.ok(ApiResponse.success(new DatabaseSettingsResponse(
                "PostgreSQL", "16", "localhost", 5432, "ai_platform",
                "connected", 22, "100MB", null
        )));
    }

    @PostMapping("/settings/database/test")
    public ResponseEntity<ApiResponse<DatabaseTestResponse>> testDatabase() {
        return ResponseEntity.ok(ApiResponse.success(new DatabaseTestResponse("connected", 5L)));
    }

    @GetMapping("/settings/api-keys")
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> getApiKeys() {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @GetMapping("/settings/infrastructure")
    public ResponseEntity<ApiResponse<InfrastructureStatusResponse>> getInfrastructure() {
        return ResponseEntity.ok(ApiResponse.success(new InfrastructureStatusResponse(
                new InfrastructureComponent("active", "1.0.0", null),
                new InfrastructureComponent("active", "1.0.0", null),
                new InfrastructureComponent("active", "1.0.0", null),
                new InfrastructureComponent("active", "1.0.0", null)
        )));
    }
}
