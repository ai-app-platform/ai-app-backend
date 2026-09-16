package com.aiplatform.domain.git;

import com.aiplatform.config.PlatformProperties;
import com.aiplatform.domain.connector.Connector;
import com.aiplatform.domain.connector.ConnectorService;
import com.aiplatform.interfaces.rest.dto.*;
import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GitManagementService {

    private final GitRepositoryRepository gitRepositoryRepository;
    private final ConnectorService connectorService;
    private final PlatformProperties platformProperties;

    @Transactional
    public GitRepository registerAndClone(UUID projectId, UUID connectorId, String remoteUrl, String defaultBranch) {
        if (gitRepositoryRepository.findByProjectId(projectId).isPresent()) {
            throw new BusinessRuleViolationException("GIT_REPO_EXISTS",
                    "Git repository already registered for this project");
        }

        String localPath = resolveLocalPath(projectId);

        GitRepository repo = GitRepository.builder()
                .projectId(projectId)
                .connectorId(connectorId)
                .remoteUrl(remoteUrl)
                .localPath(localPath)
                .defaultBranch(defaultBranch != null ? defaultBranch : "main")
                .cloneStatus(CloneStatus.CLONING)
                .build();

        repo = gitRepositoryRepository.save(repo);

        try {
            performClone(remoteUrl, localPath, defaultBranch, connectorId);
            repo.markAsCloned(localPath);
            repo.setCurrentBranch(defaultBranch != null ? defaultBranch : "main");
            repo.updateLastFetch();
            return gitRepositoryRepository.save(repo);
        } catch (Exception e) {
            repo.setCloneStatus(CloneStatus.FAILED);
            gitRepositoryRepository.save(repo);
            throw new BusinessRuleViolationException("GIT_CLONE_FAILED",
                    "Failed to clone repository: " + e.getMessage());
        }
    }

    private void performClone(String remoteUrl, String localPath, String branch, UUID connectorId)
            throws GitAPIException, IOException {
        Path path = Path.of(localPath);
        Files.createDirectories(path);

        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(path.toFile());

        if (branch != null) {
            cloneCommand.setBranch(branch);
        }

        // Resolve credentials from connector
        String credential = resolveCredential(connectorId);
        if (credential != null) {
            cloneCommand.setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider(credential, ""));
        }

        try (Git git = cloneCommand.call()) {
            log.info("Repository cloned successfully to: {}", localPath);
        }
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

    @Transactional
    public String createTaskBranch(UUID projectId, String taskBranchName) {
        GitRepository repo = findByProjectOrThrow(projectId);
        if (!repo.isCloned()) {
            throw new BusinessRuleViolationException("REPO_NOT_CLONED",
                    "Repository must be cloned before creating branches");
        }

        try (Git git = Git.open(new File(repo.getLocalPath()))) {
            git.checkout()
                    .setCreateBranch(true)
                    .setName(taskBranchName)
                    .call();

            repo.setCurrentBranch(taskBranchName);
            gitRepositoryRepository.save(repo);
            log.info("Created task branch: {}", taskBranchName);
            return taskBranchName;
        } catch (Exception e) {
            throw new BusinessRuleViolationException("BRANCH_CREATION_FAILED",
                    "Failed to create branch: " + e.getMessage());
        }
    }

    public List<BranchResponse> getBranches(UUID projectId) {
        GitRepository repo = findByProjectOrThrow(projectId);
        try (Git git = Git.open(new File(repo.getLocalPath()))) {
            List<Ref> refs = git.branchList().call();
            String defaultBranch = repo.getDefaultBranch();

            return refs.stream()
                    .map(ref -> {
                        String name = ref.getName().replace("refs/heads/", "");
                        return new BranchResponse(
                                name,
                                name.equals(defaultBranch),
                                ref.getObjectId().getName().substring(0, 7),
                                0,
                                0
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("GIT_OPERATION_FAILED",
                    "Failed to list branches: " + e.getMessage());
        }
    }

    public List<CommitResponse> getCommits(UUID projectId, String branch) {
        GitRepository repo = findByProjectOrThrow(projectId);
        try (Git git = Git.open(new File(repo.getLocalPath()))) {
            LogCommand logCommand = git.log();
            if (branch != null) {
                logCommand.add(git.getRepository().resolve(branch));
            }

            return StreamSupport.stream(logCommand.call().spliterator(), false)
                    .limit(50)
                    .map(commit -> new CommitResponse(
                            commit.getName().substring(0, 7),
                            commit.getFullMessage(),
                            commit.getAuthorIdent().getName(),
                            commit.getAuthorIdent().getWhen().toInstant().toString(),
                            branch != null ? branch : repo.getCurrentBranch()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessRuleViolationException("GIT_OPERATION_FAILED",
                    "Failed to list commits: " + e.getMessage());
        }
    }

    public DiffResponse getDiff(UUID projectId, String branch, String baseBranch) {
        GitRepository repo = findByProjectOrThrow(projectId);
        String base = baseBranch != null ? baseBranch : repo.getDefaultBranch();

        try (Git git = Git.open(new File(repo.getLocalPath()))) {
            Repository jgitRepo = git.getRepository();
            ObjectId baseId = jgitRepo.resolve(base);
            ObjectId branchId = jgitRepo.resolve(branch);

            if (baseId == null || branchId == null) {
                throw new BusinessRuleViolationException("BRANCH_NOT_FOUND", "Branch not found");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (DiffFormatter df = new DiffFormatter(out)) {
                df.setRepository(jgitRepo);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);

                List<DiffEntry> diffs = df.scan(
                        new org.eclipse.jgit.treewalk.FileTreeIterator(jgitRepo, baseId, jgitRepo.newObjectReader()),
                        new org.eclipse.jgit.treewalk.FileTreeIterator(jgitRepo, branchId, jgitRepo.newObjectReader())
                );

                List<DiffFile> files = diffs.stream()
                        .map(diff -> {
                            String path = diff.getChangeType() == DiffEntry.ChangeType.DELETE
                                    ? diff.getOldPath() : diff.getNewPath();
                            String status = diff.getChangeType().name().toLowerCase();
                            EditList edits = df.toFileHeader(diff).toEditList();
                            int additions = (int) edits.stream().filter(e -> !e.getBeginA().equals(e.getEndA())).count();
                            int deletions = (int) edits.stream().filter(e -> !e.getBeginB().equals(e.getEndB())).count();
                            return new DiffFile(path, status, additions, deletions);
                        })
                        .collect(Collectors.toList());

                for (DiffEntry diff : diffs) {
                    df.format(diff);
                }
                df.flush();

                return new DiffResponse(branch, base, files, out.toString());
            }
        } catch (Exception e) {
            throw new BusinessRuleViolationException("GIT_OPERATION_FAILED",
                    "Failed to get diff: " + e.getMessage());
        }
    }

    @Transactional
    public String commitAndPush(UUID projectId, String branch, String message) {
        GitRepository repo = findByProjectOrThrow(projectId);

        try (Git git = Git.open(new File(repo.getLocalPath()))) {
            git.add().addFilepattern(".").call();

            PersonIdent author = new PersonIdent(
                    platformProperties.getGit().getDefaultAuthorName(),
                    platformProperties.getGit().getDefaultAuthorEmail()
            );

            RevCommit commit = git.commit()
                    .setMessage(message)
                    .setAuthor(author)
                    .call();

            String credential = resolveCredential(repo.getConnectorId());
            PushCommand pushCommand = git.push();
            if (credential != null) {
                pushCommand.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(credential, ""));
            }
            pushCommand.call();

            repo.setLastCommitSha(commit.getName());
            gitRepositoryRepository.save(repo);

            log.info("Committed and pushed: {} on branch {}", commit.getName().substring(0, 7), branch);
            return commit.getName();
        } catch (Exception e) {
            throw new BusinessRuleViolationException("GIT_COMMIT_FAILED",
                    "Failed to commit and push: " + e.getMessage());
        }
    }

    public String createPullRequest(UUID projectId, String sourceBranch, String targetBranch, String title) {
        GitRepository repo = findByProjectOrThrow(projectId);
        // In production, this would use the connector (GitHub/GitLab API) to create a PR
        // For now, we simulate the PR creation
        String prUrl = repo.getRemoteUrl().replace(".git", "/pull/new/" + sourceBranch);
        log.info("Pull request created: {} -> {} for project {}", sourceBranch, targetBranch, projectId);
        return prUrl;
    }

    public Optional<GitRepository> findByProject(UUID projectId) {
        return gitRepositoryRepository.findByProjectId(projectId);
    }

    public GitRepository findByProjectOrThrow(UUID projectId) {
        return gitRepositoryRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("GitRepository", projectId));
    }

    private String resolveLocalPath(UUID projectId) {
        return platformProperties.getWorkspace().getBasePath() + "/" + projectId.toString();
    }

    private String resolveCredential(UUID connectorId) {
        if (connectorId == null) return null;
        try {
            Connector connector = connectorService.findByIdOrThrow(connectorId);
            // In production, this would resolve from a secret vault
            // The credentialRef points to a secret, not the actual credential
            return null; // Placeholder - real implementation uses Vault/Secret Manager
        } catch (Exception e) {
            return null;
        }
    }
}
