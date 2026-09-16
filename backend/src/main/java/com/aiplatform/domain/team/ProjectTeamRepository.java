package com.aiplatform.domain.team;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectTeamRepository extends BaseRepository<ProjectTeam> {
    Optional<ProjectTeam> findByProjectId(UUID projectId);
    boolean existsByProjectId(UUID projectId);
}
