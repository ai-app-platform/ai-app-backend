package com.aiplatform.interfaces.rest.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// ============ Dashboard DTOs ============
public record DashboardStatsResponse(
    int totalProjects,
    int activeTasks,
    int completedTasks,
    int totalAgents,
    int totalTools,
    int totalConnectors,
    double successRate,
    String avgExecutionTime
) {}

public record ActivityResponse(
    String id,
    String type,
    String message,
    String project,
    String time,
    String icon
) {}

// ============ Project DTOs ============
public record ProjectResponse(
    UUID id,
    String name,
    String description,
    String status,
    String language,
    String gitUrl,
    String lastActivity,
    int agents,
    int tasks,
    String workspace
) {}

public record ProjectDetailResponse(
    UUID id,
    String name,
    String description,
    String status,
    String language,
    String gitUrl,
    String createdAt,
    String updatedAt,
    String connector,
    String workspace,
    UUID teamId,
    UUID defaultWorkflow,
    ProjectStats stats,
    List<CommitInfo> recentCommits,
    Map<String, String> environment
) {}

public record ProjectStats(
    int totalFiles,
    int totalLines,
    int totalTasks,
    int completedTasks,
    int activeAgents
) {}

public record CommitInfo(
    String sha,
    String message,
    String author,
    String date
) {}

public record CreateProjectRequest(
    String name,
    String description,
    String language,
    String gitUrl,
    UUID connectorId,
    UUID workflowId
) {}

public record UpdateProjectRequest(
    String name,
    String description,
    String status,
    String language,
    String gitUrl,
    UUID connectorId,
    UUID workflowId
) {}

// ============ Task DTOs ============
public record TaskResponse(
    UUID id,
    String title,
    UUID projectId,
    String status,
    String priority,
    List<String> assignedAgents,
    String workflow,
    String createdAt,
    String completedAt
) {}

public record TaskDetailResponse(
    UUID id,
    String title,
    UUID projectId,
    String status,
    String priority,
    List<String> assignedAgents,
    String workflow,
    String createdAt,
    String completedAt,
    String description,
    List<String> requirements,
    List<ExecutionStep> executionSteps,
    GitInfo gitInfo,
    ValidationInfo validation
) {}

public record ExecutionStep(
    String step,
    String agent,
    String status,
    String output
) {}

public record GitInfo(
    String branch,
    int commits,
    int filesChanged
) {}

public record ValidationInfo(
    Boolean buildPassed,
    Boolean testsPassed,
    Boolean securityScan,
    String codeQuality
) {}

public record CreateTaskRequest(
    String title,
    String description,
    UUID projectId,
    String priority,
    UUID workflowId,
    List<String> requirements,
    List<String> assignedAgents
) {}

public record UpdateTaskStatusRequest(
    String status
) {}

// ============ Agent DTOs ============
public record AgentResponse(
    UUID id,
    String name,
    String type,
    String status,
    String model,
    List<String> skills,
    List<String> roles,
    String description,
    String version
) {}

public record AgentDetailResponse(
    UUID id,
    String name,
    String type,
    String status,
    String model,
    List<String> skills,
    List<String> roles,
    String description,
    String version,
    AgentConfiguration configuration,
    List<String> allowedTools,
    ExecutionPolicy executionPolicy,
    String promptRef,
    String createdAt,
    String lastUsed,
    AgentStats stats
) {}

public record AgentConfiguration(
    Double temperature,
    Integer maxTokens,
    Double topP
) {}

public record ExecutionPolicy(
    Integer maxRetries,
    Integer timeout,
    Boolean approvalRequired
) {}

public record AgentStats(
    int totalTasks,
    double successRate,
    String avgDuration
) {}

public record CreateAgentRequest(
    String name,
    String description,
    String type,
    String model,
    List<String> skills,
    List<String> roles,
    AgentConfiguration configuration,
    List<String> allowedTools,
    ExecutionPolicy executionPolicy
) {}

public record UpdateAgentRequest(
    String name,
    String description,
    String status,
    String model,
    List<String> skills,
    List<String> roles,
    AgentConfiguration configuration,
    List<String> allowedTools,
    ExecutionPolicy executionPolicy
) {}

// ============ Tool DTOs ============
public record ToolResponse(
    UUID id,
    String name,
    String type,
    String description,
    String status,
    String version,
    List<String> capabilities
) {}

public record ToolDetailResponse(
    UUID id,
    String name,
    String type,
    String description,
    String status,
    String version,
    List<String> capabilities,
    Map<String, String> inputSchema,
    Map<String, String> outputSchema,
    ToolPermissions permissions,
    ExecutionPolicy executionPolicy,
    ToolUsageStats usageStats
) {}

public record ToolPermissions(
    boolean required,
    List<String> roles
) {}

public record ToolUsageStats(
    int totalCalls,
    String avgLatency,
    double successRate
) {}

public record CreateToolRequest(
    String name,
    String description,
    String type,
    Map<String, String> inputSchema,
    Map<String, String> outputSchema,
    List<String> capabilities,
    ToolPermissions permissions,
    ExecutionPolicy executionPolicy
) {}

// ============ Connector DTOs ============
public record ConnectorResponse(
    UUID id,
    String name,
    String provider,
    String type,
    String status,
    String repository,
    List<String> capabilities,
    String healthCheck
) {}

public record ConnectorDetailResponse(
    UUID id,
    String name,
    String provider,
    String type,
    String status,
    String repository,
    String endpoint,
    String credentialRef,
    String adapter,
    List<String> capabilities,
    ConnectorPermissions permissions,
    HealthCheckInfo healthCheck,
    String createdAt,
    String lastSync
) {}

public record ConnectorPermissions(
    boolean read,
    boolean write
) {}

public record HealthCheckInfo(
    String status,
    String lastCheck,
    Long latency
) {}

public record CreateConnectorRequest(
    String name,
    String provider,
    String type,
    String endpoint,
    String repository,
    String adapter,
    String credentialRef,
    ConnectorPermissions permissions
) {}

public record ConnectorTestResponse(
    UUID id,
    String status,
    Long latency
) {}

// ============ Knowledge DTOs ============
public record KnowledgeResponse(
    UUID id,
    String title,
    String scope,
    String version,
    String category,
    String lastUpdated,
    String status,
    UUID projectId
) {}

public record KnowledgeDetailResponse(
    UUID id,
    String title,
    String scope,
    String version,
    String category,
    String lastUpdated,
    String status,
    String content,
    List<String> references,
    List<String> usedByProjects,
    List<ChangeHistory> changeHistory
) {}

public record ChangeHistory(
    String version,
    String date,
    String changes
) {}

public record CreateKnowledgeRequest(
    String title,
    String content,
    String scope,
    String category,
    UUID projectId,
    List<String> references
) {}

// ============ Workflow DTOs ============
public record WorkflowResponse(
    UUID id,
    String name,
    String description,
    List<String> steps,
    String status,
    String version,
    boolean allowParallel
) {}

public record WorkflowDetailResponse(
    UUID id,
    String name,
    String description,
    List<String> steps,
    String status,
    String version,
    boolean allowParallel,
    WorkflowValidation validation,
    WorkflowConfiguration configuration,
    List<StepDetail> stepDetails
) {}

public record WorkflowValidation(
    boolean required,
    boolean autoApprove
) {}

public record WorkflowConfiguration(
    PlanningConfig planning,
    ExecutionConfig execution,
    ApprovalConfig approval
) {}

public record PlanningConfig(
    boolean enabled,
    int maxIterations
) {}

public record ExecutionConfig(
    boolean allowRetry,
    int maxRetries
) {}

public record ApprovalConfig(
    boolean required,
    List<String> approvers
) {}

public record StepDetail(
    String name,
    String description,
    int timeout
) {}

public record CreateWorkflowRequest(
    String name,
    String description,
    List<String> steps,
    boolean allowParallel,
    WorkflowValidation validation,
    WorkflowConfiguration configuration
) {}

// ============ Role DTOs ============
public record RoleResponse(
    UUID id,
    String name,
    String description,
    List<String> constraints,
    List<String> capabilities
) {}

public record RoleDetailResponse(
    UUID id,
    String name,
    String description,
    List<String> constraints,
    List<String> capabilities,
    List<String> allowedTools,
    String promptTemplate,
    List<String> assignedAgents
) {}

public record CreateRoleRequest(
    String name,
    String description,
    List<String> constraints,
    List<String> capabilities,
    List<String> allowedTools,
    String promptTemplate
) {}

// ============ Skill DTOs ============
public record SkillResponse(
    UUID id,
    String name,
    String category,
    String level,
    String description
) {}

public record SkillDetailResponse(
    UUID id,
    String name,
    String category,
    String level,
    String description,
    List<String> subSkills,
    List<String> usedByAgents,
    SkillAssessment assessment
) {}

public record SkillAssessment(
    double score,
    String lastAssessed
) {}

public record CreateSkillRequest(
    String name,
    String description,
    String category,
    String level,
    List<String> subSkills
) {}

// ============ Team DTOs ============
public record TeamResponse(
    UUID id,
    UUID projectId,
    String name,
    String version,
    int agentCount
) {}

public record TeamDetailResponse(
    UUID id,
    UUID projectId,
    String name,
    String version,
    List<TeamAgent> agents,
    String createdAt,
    String lastModified,
    String workflow
) {}

public record TeamAgent(
    UUID agentId,
    String role,
    boolean enabled,
    Object configuration
) {}

public record CreateTeamRequest(
    String name,
    UUID projectId,
    UUID workflowId,
    List<TeamAgent> agents
) {}

public record UpdateTeamRequest(
    String name,
    List<TeamAgent> agents
) {}

// ============ Prompt DTOs ============
public record PromptResponse(
    UUID id,
    String name,
    String version,
    String type,
    String status,
    List<String> variables
) {}

public record PromptDetailResponse(
    UUID id,
    String name,
    String version,
    String type,
    String status,
    List<String> variables,
    String template,
    List<String> usedByAgents,
    ModelConfig modelConfig,
    List<ChangeHistory> changeHistory
) {}

public record ModelConfig(
    String model,
    Double temperature,
    Integer maxTokens
) {}

public record CreatePromptRequest(
    String name,
    String template,
    String type,
    List<String> variables,
    ModelConfig modelConfig
) {}

// ============ Memory DTOs ============
public record MemoryResponse(
    UUID id,
    UUID projectId,
    UUID taskId,
    String type,
    String content,
    String timestamp,
    Map<String, Object> metadata,
    boolean promoted,
    boolean ragIndexed
) {}

// ============ RAG DTOs ============
public record RagStatusResponse(
    int totalDocuments,
    String lastIndexed,
    String status,
    String embeddingModel,
    String indexVersion
) {}

public record RagSearchRequest(
    String query,
    Integer limit,
    Map<String, Object> filters
) {}

public record RagSearchResult(
    UUID id,
    String source,
    double relevance,
    String content,
    String type,
    String lineRange,
    Map<String, String> metadata,
    List<RagChunk> chunks
) {}

public record RagChunk(
    String text,
    double score
) {}

// ============ Git DTOs ============
public record BranchResponse(
    String name,
    boolean isDefault,
    String lastCommit,
    int ahead,
    int behind
) {}

public record CreateBranchRequest(
    String name,
    String baseBranch
) {}

public record CommitResponse(
    String sha,
    String message,
    String author,
    String date,
    String branch
) {}

public record PullRequestResponse(
    String id,
    String title,
    String branch,
    String status,
    List<String> reviewers,
    String createdAt,
    int comments
) {}

public record DiffResponse(
    String branch,
    String baseBranch,
    List<DiffFile> files,
    String diffContent
) {}

public record DiffFile(
    String path,
    String status,
    int additions,
    int deletions
) {}

// ============ Codebase Intelligence DTOs ============
public record CodebaseOverviewResponse(
    int totalFiles,
    int totalLines,
    Map<String, Integer> languages,
    int modules,
    String lastAnalysis,
    String status
) {}

public record ModuleResponse(
    String name,
    int files,
    int classes,
    List<String> dependencies
) {}

public record ModuleDetailResponse(
    String name,
    int files,
    int classes,
    List<String> dependencies,
    List<String> filesList
) {}

public record CodebaseSearchRequest(
    String query,
    String type
) {}

public record CodebaseSearchResult(
    String file,
    int line,
    String content,
    String symbol
) {}

public record SymbolDetailResponse(
    String name,
    String type,
    String file,
    int line,
    List<String> methods,
    List<String> dependencies,
    List<String> callers
) {}

public record FileContentResponse(
    String path,
    String content
) {}

// ============ Context Engine DTOs ============
public record ContextResponse(
    String assembledAt,
    Map<String, Integer> sources,
    int totalTokens,
    int maxTokens,
    String retrievalStrategy,
    List<ContextSection> sections
) {}

public record ContextSection(
    String type,
    int tokens,
    String source
) {}

// ============ Workspace DTOs ============
public record WorkspaceResponse(
    String name,
    String status,
    String size,
    String lastSync,
    int files,
    UUID projectId
) {}

public record WorkspaceDetailResponse(
    String name,
    String status,
    String size,
    String lastSync,
    int files,
    String directory,
    String buildStatus,
    String lastBuild,
    TestResults testResults,
    List<String> artifacts,
    Map<String, String> environment
) {}

public record TestResults(
    int total,
    int passed,
    int failed,
    double coverage
) {}

public record WorkspaceFileResponse(
    String name,
    String type,
    String size,
    String modified
) {}

public record ExecuteCommandRequest(
    String command
) {}

public record ExecuteCommandResponse(
    String output,
    int exitCode
) {}

// ============ Settings DTOs ============
public record DatabaseSettingsResponse(
    String type,
    String version,
    String host,
    int port,
    String name,
    String status,
    int tables,
    String size,
    String lastBackup
) {}

public record DatabaseTestResponse(
    String status,
    Long latency
) {}

public record DatabaseBackupResponse(
    String status,
    String size,
    String timestamp
) {}

public record ApiKeyResponse(
    UUID id,
    String name,
    String provider,
    String status,
    String lastUsed,
    String masked
) {}

public record CreateApiKeyRequest(
    String name,
    String provider,
    String key
) {}

public record InfrastructureStatusResponse(
    InfrastructureComponent langgraph,
    InfrastructureComponent rag,
    InfrastructureComponent codebase,
    InfrastructureComponent contextEngine
) {}

public record InfrastructureComponent(
    String status,
    String version,
    Object details
) {}
