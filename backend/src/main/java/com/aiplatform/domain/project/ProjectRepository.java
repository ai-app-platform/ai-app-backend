package com.aiplatform.domain.project;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends BaseRepository<Project> {
    Optional<Project> findByName(String name);
    boolean existsByName(String name);
    Page<Project> findByStatus(com.aiplatform.shared.domain.EntityStatus status, Pageable pageable);
}
