package com.aiplatform.domain.git;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GitRepositoryRepository extends BaseRepository<GitRepository> {
    Optional<GitRepository> findByProjectId(UUID projectId);
    Optional<GitRepository> findByRemoteUrl(String remoteUrl);
}
