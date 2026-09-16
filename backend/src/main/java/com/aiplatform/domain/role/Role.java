package com.aiplatform.domain.role;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_roles_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Role extends AggregateRoot {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "responsibility", columnDefinition = "TEXT")
    private String responsibility;

    @Column(name = "constraints", columnDefinition = "TEXT")
    private String constraints;

    @Column(name = "prompt_policy_ref")
    private String promptPolicyRef;

    @Column(name = "skill_policy_ref")
    private String skillPolicyRef;

    @Column(name = "tool_policy_ref")
    private String toolPolicyRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Override
    public String getAggregateType() {
        return "ROLE";
    }
}
