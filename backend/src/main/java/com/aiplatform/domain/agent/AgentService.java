package com.aiplatform.domain.agent;

import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import com.aiplatform.shared.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentService implements CrudService<Agent, AgentCreateCommand, AgentUpdateCommand> {

    private final AgentRepository agentRepository;

    @Override
    @Transactional
    public Agent create(AgentCreateCommand command) {
        validateAgentName(command.name());

        Agent agent = Agent.builder()
                .name(command.name())
                .description(command.description())
                .agentType(command.agentType())
                .capabilities(command.capabilities())
                .defaultPromptRef(command.defaultPromptRef())
                .modelConfiguration(command.modelConfiguration())
                .runtimeConfiguration(command.runtimeConfiguration())
                .executionPolicies(command.executionPolicies())
                .versionInfo(com.aiplatform.shared.domain.VersionInfo.initial())
                .build();

        return agentRepository.save(agent);
    }

    @Override
    @Transactional
    public Agent update(UUID id, AgentUpdateCommand command) {
        Agent agent = findByIdOrThrow(id);

        if (command.description() != null) agent.setDescription(command.description());
        if (command.capabilities() != null) agent.setCapabilities(command.capabilities());
        if (command.defaultPromptRef() != null) agent.setDefaultPromptRef(command.defaultPromptRef());
        if (command.modelConfiguration() != null) agent.setModelConfiguration(command.modelConfiguration());
        if (command.runtimeConfiguration() != null) agent.setRuntimeConfiguration(command.runtimeConfiguration());
        if (command.executionPolicies() != null) agent.setExecutionPolicies(command.executionPolicies());

        return agentRepository.save(agent);
    }

    @Override
    public Optional<Agent> findById(UUID id) {
        return agentRepository.findById(id);
    }

    @Override
    public List<Agent> findAll(int page, int size) {
        return agentRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Agent agent = findByIdOrThrow(id);
        agent.setStatus(com.aiplatform.shared.domain.EntityStatus.ARCHIVED);
        agentRepository.save(agent);
    }

    public Agent findByIdOrThrow(UUID id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agent", id));
    }

    public List<Agent> findPlatformAgents() {
        return agentRepository.findByAgentType(AgentType.PLATFORM);
    }

    @Transactional
    public void assignSkill(UUID agentId, UUID skillId) {
        Agent agent = findByIdOrThrow(agentId);
        agent.addSkill(skillId);
        agentRepository.save(agent);
    }

    @Transactional
    public void removeSkill(UUID agentId, UUID skillId) {
        Agent agent = findByIdOrThrow(agentId);
        agent.removeSkill(skillId);
        agentRepository.save(agent);
    }

    @Transactional
    public void assignTool(UUID agentId, UUID toolId) {
        Agent agent = findByIdOrThrow(agentId);
        agent.addTool(toolId);
        agentRepository.save(agent);
    }

    @Transactional
    public void publishNewVersion(UUID agentId) {
        Agent agent = findByIdOrThrow(agentId);
        agent.publishNewVersion();
        agentRepository.save(agent);
    }

    private void validateAgentName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("AGENT_NAME_REQUIRED", "Agent name is required");
        }
        if (agentRepository.existsByName(name)) {
            throw new BusinessRuleViolationException("AGENT_NAME_UNIQUE",
                    String.format("Agent with name '%s' already exists", name));
        }
    }
}
