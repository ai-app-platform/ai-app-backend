package com.aiplatform.domain.git;

import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GitManagementService {

    private final GitRepositoryRepository gitRepositoryRepository;

    @Transactional
    public GitRepository registerRepository(UUID projectId, UUID connectorId, String remoteUrl, String defaultBranch) {
        if (gitRepositoryRepository.findByProjectId(projectId).isPresent()) {
            throw new BusinessRuleViolationException("GIT_REPO_EXISTS",
                    "Git repository already registered for this project");
        }

        GitRepository repo = GitRepository.builder()
                .projectId(projectId)
                .connectorId(connectorId)
                .remoteUrl(remoteUrl)
                .defaultBranch(defaultBranch != null ? defaultBranch : "main")
                .build();

        return gitRepositoryRepository.save(repo);
    }

    public Optional<GitRepository> findByProject(UUID projectId) {
        return gitRepositoryRepository.findByProjectId(projectId);
    }

    public GitRepository findByProjectOrThrow(UUID projectId) {
        return gitRepositoryRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("GitRepository", projectId));
    }

    @Transactional
    public GitRepository markAsCloned(UUID projectId, String localPath) {
        GitRepository repo = findByProjectOrThrow(projectId);
        repo.markAsCloned(localPath);
        return gitRepositoryRepository.save(repo);
    }

    @Transactional
    public GitRepository updateLastFetch(UUID projectId) {
        GitRepository repo = findByProjectOrThrow(projectId);
        repo.updateLastFetch();
        return gitRepositoryRepository.save(repo);
    }

    @Transactional
    public GitRepository updateCurrentBranch(UUID projectId, String branch) {
        GitRepository repo = findByProjectOrThrow(projectId);
        repo.setCurrentBranch(branch);
        return gitRepositoryRepository.save(repo);
    }

    @Transactional
    public GitRepository updateLastCommit(UUID projectId, String commitSha) {
        GitRepository repo = findByProjectOrThrow(projectId);
        repo.setLastCommitSha(commitSha);
        return gitRepositoryRepository.save(repo);
    }

    /**
     * Creates a task branch for the given task.
     * In a real implementation, this would use JGit to create the branch.
     */
    @Transactional
    public String createTaskBranch(UUID projectId, String taskBranchName) {
        GitRepository repo = findByProjectOrThrow(projectId);
        if (!repo.isCloned()) {
            throw new BusinessRuleViolationException("REPO_NOT_CLONED",
                    "Repository must be cloned before creating branches");
        }
        repo.setCurrentBranch(taskBranchName);
        gitRepositoryRepository.save(repo);
        return taskBranchName;
    }

    /**
     * Prepares commit, push, and PR information.
     * In a real implementation, this would use JGit + Connector.
     */
    public CommitInfo prepareCommit(UUID projectId, String message, String taskBranch) {
        GitRepository repo = findByProjectOrThrow(projectId);
        return new CommitInfo(
                repo.getRemoteUrl(),
                taskBranch,
                message,
                repo.getLastCommitSha()
        );
    }

    public record CommitInfo(String remoteUrl, String branch, String message, String parentSha) {}
}
