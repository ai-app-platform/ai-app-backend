package com.aiplatform.domain.agent;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import com.aiplatform.shared.domain.VersionInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "agents", indexes = {
    @Index(name = "idx_agents_name", columnList = "name"),
    @Index(name = "idx_agents_type", columnList = "agent_type"),
    @Index(name = "idx_agents_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Agent extends AggregateRoot {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "agent_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AgentType agentType;

    @Column(name = "capabilities", columnDefinition = "TEXT")
    private String capabilities;

    @Column(name = "default_prompt_ref")
    private String defaultPromptRef;

    @Column(name = "model_configuration", columnDefinition = "jsonb")
    private String modelConfiguration;

    @Column(name = "runtime_configuration", columnDefinition = "jsonb")
    private String runtimeConfiguration;

    @Column(name = "execution_policies", columnDefinition = "jsonb")
    private String executionPolicies;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "semanticVersion", column = @Column(name = "version_semantic")),
        @AttributeOverride(name = "latest", column = @Column(name = "version_latest")),
        @AttributeOverride(name = "versionSequence", column = @Column(name = "version_sequence"))
    })
    private VersionInfo versionInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agent_skill_refs", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "skill_id")
    @Builder.Default
    private Set<UUID> skillRefs = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "agent_tool_refs", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "tool_id")
    @Builder.Default
    private Set<UUID> toolRefs = new HashSet<>();

    @Override
    public String getAggregateType() {
        return "AGENT";
    }

    public boolean isPlatformAgent() {
        return this.agentType == AgentType.PLATFORM;
    }

    public void addSkill(UUID skillId) {
        this.skillRefs.add(skillId);
    }

    public void removeSkill(UUID skillId) {
        this.skillRefs.remove(skillId);
    }

    public void addTool(UUID toolId) {
        this.toolRefs.add(toolId);
    }

    public void removeTool(UUID toolId) {
        this.toolRefs.remove(toolId);
    }

    public void publishNewVersion() {
        this.versionInfo = this.versionInfo.increment();
    }
}
