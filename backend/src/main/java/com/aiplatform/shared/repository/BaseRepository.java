package com.aiplatform.shared.repository;

import com.aiplatform.shared.domain.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, UUID> {
    Optional<T> findByIdAndStatusNot(UUID id, String status);
}
