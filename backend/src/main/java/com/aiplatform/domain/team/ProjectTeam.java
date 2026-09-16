package com.aiplatform.domain.team;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import com.aiplatform.shared.domain.VersionInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "project_teams", indexes = {
    @Index(name = "idx_teams_project", columnList = "project_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProjectTeam extends AggregateRoot {

    @Column(name = "project_id", nullable = false, unique = true)
    private UUID projectId;

    @Column(name = "name")
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_team_agents", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "agent_id")
    @Builder.Default
    private Set<UUID> agentIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_team_roles", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "role_id")
    @Builder.Default
    private Set<UUID> roleIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_team_disabled_agents", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "agent_id")
    @Builder.Default
    private Set<UUID> disabledAgentIds = new HashSet<>();

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

    @Override
    public String getAggregateType() {
        return "PROJECT_TEAM";
    }

    public void addAgent(UUID agentId) {
        this.agentIds.add(agentId);
        this.disabledAgentIds.remove(agentId);
    }

    public void removeAgent(UUID agentId) {
        this.agentIds.remove(agentId);
        this.disabledAgentIds.remove(agentId);
    }

    public void disableAgent(UUID agentId) {
        if (this.agentIds.contains(agentId)) {
            this.disabledAgentIds.add(agentId);
        }
    }

    public void enableAgent(UUID agentId) {
        this.disabledAgentIds.remove(agentId);
    }

    public boolean isAgentEnabled(UUID agentId) {
        return this.agentIds.contains(agentId) && !this.disabledAgentIds.contains(agentId);
    }

    public Set<UUID> getEnabledAgents() {
        Set<UUID> enabled = new HashSet<>(this.agentIds);
        enabled.removeAll(this.disabledAgentIds);
        return enabled;
    }

    public void publishNewVersion() {
        this.versionInfo = this.versionInfo.increment();
    }
}
