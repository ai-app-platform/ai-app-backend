package com.aiplatform.domain.skill;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends BaseRepository<Skill> {
    Optional<Skill> findByName(String name);
    boolean existsByName(String name);
    List<Skill> findByCategory(String category);
}
