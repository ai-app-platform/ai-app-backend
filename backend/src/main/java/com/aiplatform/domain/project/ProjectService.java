package com.aiplatform.domain.project;

import com.aiplatform.domain.git.GitManagementService;
import com.aiplatform.domain.memory.ShortTermMemoryService;
import com.aiplatform.domain.rag.RagService;
import com.aiplatform.domain.workspace.Workspace;
import com.aiplatform.domain.workspace.WorkspaceService;
import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final GitManagementService gitManagementService;
    private final WorkspaceService workspaceService;
    private final RagService ragService;
    private final ShortTermMemoryService shortTermMemoryService;

    @Transactional
    public Project create(ProjectCreateCommand command) {
        validateProjectName(command.name());

        Project project = Project.builder()
                .name(command.name())
                .description(command.description())
                .repositoryUrl(command.repositoryUrl())
                .defaultBranch(command.defaultBranch() != null ? command.defaultBranch() : "main")
                .connectorId(command.connectorId())
                .configuration(command.configuration())
                .build();

        project = projectRepository.save(project);

        // Initialize workspace
        String workspacePath = initializeWorkspace(project.getId(), command.repositoryUrl());
        project.setWorkspacePath(workspacePath);
        project = projectRepository.save(project);

        // Clone repository and start indexing asynchronously
        cloneAndIndexAsync(project.getId(), command.connectorId(), command.repositoryUrl(),
                command.defaultBranch(), workspacePath);

        return project;
    }

    @Async
    public void cloneAndIndexAsync(UUID projectId, UUID connectorId, String remoteUrl,
                                    String defaultBranch, String workspacePath) {
        try {
            // 1. Clone repository
            gitManagementService.registerAndClone(projectId, connectorId, remoteUrl, defaultBranch);
            log.info("Repository cloned for project: {}", projectId);

            // 2. Initialize .ai directory
            shortTermMemoryService.initializeAiDirectory(workspacePath, projectId);

            // 3. Generate short-term memory files
            shortTermMemoryService.generateProjectOverview(workspacePath, projectId);
            shortTermMemoryService.generateStackInfo(workspacePath, projectId);
            shortTermMemoryService.generateStructureInfo(workspacePath, projectId);
            shortTermMemoryService.generateModulesInfo(workspacePath, projectId);
            shortTermMemoryService.generateClassesInfo(workspacePath, projectId);
            shortTermMemoryService.generateDependenciesInfo(workspacePath, projectId);
            log.info("Short-term memory generated for project: {}", projectId);

            // 4. Get current commit SHA
            String commitSha = gitManagementService.findByProjectOrThrow(projectId).getLastCommitSha();
            if (commitSha == null) {
                commitSha = "initial";
            }

            // 5. Index project in RAG (long-term memory)
            ragService.indexProject(projectId, workspacePath, commitSha);
            log.info("RAG indexing completed for project: {}", projectId);

        } catch (Exception e) {
            log.error("Failed to clone and index project: {}", projectId, e);
        }
    }

    @Transactional
    public Project update(UUID id, ProjectUpdateCommand command) {
        Project project = findByIdOrThrow(id);

        if (command.name() != null && !command.name().equals(project.getName())) {
            validateProjectName(command.name());
            project.setName(command.name());
        }
        if (command.description() != null) project.setDescription(command.description());
        if (command.repositoryUrl() != null) project.setRepositoryUrl(command.repositoryUrl());
        if (command.defaultBranch() != null) project.setDefaultBranch(command.defaultBranch());
        if (command.configuration() != null) project.setConfiguration(command.configuration());

        return projectRepository.save(project);
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return projectRepository.findById(id);
    }

    public List<Project> findAll(int page, int size) {
        return projectRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Transactional
    public void delete(UUID id) {
        Project project = findByIdOrThrow(id);
        project.archive();
        projectRepository.save(project);
    }

    public Project findByIdOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project", id));
    }

    public Page<Project> findActiveProjects(int page, int size) {
        return projectRepository.findByStatus(
                com.aiplatform.shared.domain.EntityStatus.ACTIVE,
                PageRequest.of(page, size)
        );
    }

    private String initializeWorkspace(UUID projectId, String repositoryUrl) {
        String basePath = "/tmp/ai-platform/workspaces/" + projectId.toString();
        Workspace workspace = workspaceService.initialize(projectId, basePath);
        return basePath;
    }

    private void validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("PROJECT_NAME_REQUIRED", "Project name is required");
        }
        if (projectRepository.existsByName(name)) {
            throw new BusinessRuleViolationException("PROJECT_NAME_UNIQUE",
                    String.format("Project with name '%s' already exists", name));
        }
    }
}
