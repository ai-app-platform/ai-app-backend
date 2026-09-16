package com.aiplatform.domain.skill;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skills", indexes = {
    @Index(name = "idx_skills_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Skill extends AggregateRoot {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "expertise_level")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExpertiseLevel expertiseLevel = ExpertiseLevel.INTERMEDIATE;

    @Column(name = "configuration", columnDefinition = "jsonb")
    private String configuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Override
    public String getAggregateType() {
        return "SKILL";
    }
}
