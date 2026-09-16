package com.aiplatform.domain.team;

import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectTeamService {

    private final ProjectTeamRepository projectTeamRepository;

    @Transactional
    public ProjectTeam createTeam(UUID projectId, String name) {
        if (projectTeamRepository.existsByProjectId(projectId)) {
            throw new BusinessRuleViolationException("TEAM_EXISTS",
                    "Project team already exists for this project");
        }

        ProjectTeam team = ProjectTeam.builder()
                .projectId(projectId)
                .name(name)
                .versionInfo(com.aiplatform.shared.domain.VersionInfo.initial())
                .build();

        return projectTeamRepository.save(team);
    }

    public Optional<ProjectTeam> findByProject(UUID projectId) {
        return projectTeamRepository.findByProjectId(projectId);
    }

    public ProjectTeam findByProjectOrThrow(UUID projectId) {
        return projectTeamRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectTeam", projectId));
    }

    @Transactional
    public ProjectTeam addAgent(UUID projectId, UUID agentId) {
        ProjectTeam team = findByProjectOrThrow(projectId);
        team.addAgent(agentId);
        team.publishNewVersion();
        return projectTeamRepository.save(team);
    }

    @Transactional
    public ProjectTeam removeAgent(UUID projectId, UUID agentId) {
        ProjectTeam team = findByProjectOrThrow(projectId);
        team.removeAgent(agentId);
        team.publishNewVersion();
        return projectTeamRepository.save(team);
    }

    @Transactional
    public ProjectTeam disableAgent(UUID projectId, UUID agentId) {
        ProjectTeam team = findByProjectOrThrow(projectId);
        team.disableAgent(agentId);
        team.publishNewVersion();
        return projectTeamRepository.save(team);
    }

    @Transactional
    public ProjectTeam enableAgent(UUID projectId, UUID agentId) {
        ProjectTeam team = findByProjectOrThrow(projectId);
        team.enableAgent(agentId);
        team.publishNewVersion();
        return projectTeamRepository.save(team);
    }

    @Transactional
    public ProjectTeam addRole(UUID projectId, UUID roleId) {
        ProjectTeam team = findByProjectOrThrow(projectId);
        team.getRoleIds().add(roleId);
        team.publishNewVersion();
        return projectTeamRepository.save(team);
    }

    @Transactional
    public ProjectTeam removeRole(UUID projectId, UUID roleId) {
        ProjectTeam team = findByProjectOrThrow(projectId);
        team.getRoleIds().remove(roleId);
        team.publishNewVersion();
        return projectTeamRepository.save(team);
    }

    public Set<UUID> getEnabledAgents(UUID projectId) {
        ProjectTeam team = findByProjectOrThrow(projectId);
        return team.getEnabledAgents();
    }
}
