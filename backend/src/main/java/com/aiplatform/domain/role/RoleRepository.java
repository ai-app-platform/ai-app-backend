package com.aiplatform.domain.role;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends BaseRepository<Role> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
