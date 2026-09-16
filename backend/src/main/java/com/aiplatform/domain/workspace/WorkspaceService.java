package com.aiplatform.domain.workspace;

import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public Workspace initialize(UUID projectId, String basePath) {
        Workspace workspace = Workspace.builder()
                .projectId(projectId)
                .basePath(basePath)
                .workspaceStatus(WorkspaceStatus.INITIALIZED)
                .build();

        return workspaceRepository.save(workspace);
    }

    @Transactional
    public Workspace prepare(UUID projectId) {
        Workspace workspace = findByProjectOrThrow(projectId);
        workspace.setWorkspaceStatus(WorkspaceStatus.PREPARING);
        // In a real implementation: clone/copy repo, set up .ai/ directory structure
        workspace.markAsReady();
        return workspaceRepository.save(workspace);
    }

    public Optional<Workspace> findByProject(UUID projectId) {
        return workspaceRepository.findByProjectId(projectId);
    }

    public Workspace findByProjectOrThrow(UUID projectId) {
        return workspaceRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace", projectId));
    }

    public String getTaskPath(UUID projectId, String taskBranch) {
        Workspace workspace = findByProjectOrThrow(projectId);
        return workspace.getTaskWorkspacePath(taskBranch);
    }

    public String getAiDirectoryPath(UUID projectId) {
        Workspace workspace = findByProjectOrThrow(projectId);
        return workspace.getAiDirectoryPath();
    }
}
