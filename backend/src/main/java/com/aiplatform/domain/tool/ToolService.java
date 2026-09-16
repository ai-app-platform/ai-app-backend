package com.aiplatform.domain.tool;

import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import com.aiplatform.shared.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ToolService implements CrudService<Tool, ToolCreateCommand, ToolUpdateCommand> {

    private final ToolRepository toolRepository;

    @Override
    @Transactional
    public Tool create(ToolCreateCommand command) {
        validateName(command.name());
        validateCapability(command.capability());

        Tool tool = Tool.builder()
                .name(command.name())
                .description(command.description())
                .toolType(command.toolType())
                .capability(command.capability())
                .inputSchema(command.inputSchema())
                .outputSchema(command.outputSchema())
                .permissionLevel(command.permissionLevel() != null ? command.permissionLevel() : PermissionLevel.STANDARD)
                .connectorId(command.connectorId())
                .executionPolicy(command.executionPolicy())
                .testingConfiguration(command.testingConfiguration())
                .versionInfo(com.aiplatform.shared.domain.VersionInfo.initial())
                .build();

        return toolRepository.save(tool);
    }

    @Override
    @Transactional
    public Tool update(UUID id, ToolUpdateCommand command) {
        Tool tool = findByIdOrThrow(id);

        if (command.description() != null) tool.setDescription(command.description());
        if (command.inputSchema() != null) tool.setInputSchema(command.inputSchema());
        if (command.outputSchema() != null) tool.setOutputSchema(command.outputSchema());
        if (command.permissionLevel() != null) tool.setPermissionLevel(command.permissionLevel());
        if (command.executionPolicy() != null) tool.setExecutionPolicy(command.executionPolicy());
        if (command.testingConfiguration() != null) tool.setTestingConfiguration(command.testingConfiguration());

        return toolRepository.save(tool);
    }

    @Override
    public Optional<Tool> findById(UUID id) {
        return toolRepository.findById(id);
    }

    @Override
    public List<Tool> findAll(int page, int size) {
        return toolRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Tool tool = findByIdOrThrow(id);
        tool.setStatus(com.aiplatform.shared.domain.EntityStatus.ARCHIVED);
        toolRepository.save(tool);
    }

    public Tool findByIdOrThrow(UUID id) {
        return toolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tool", id));
    }

    public List<Tool> findByCapability(String capability) {
        return toolRepository.findByCapability(capability);
    }

    public List<Tool> resolveToolsForConnector(UUID connectorId) {
        return toolRepository.findByConnectorId(connectorId);
    }

    @Transactional
    public void publishNewVersion(UUID toolId) {
        Tool tool = findByIdOrThrow(toolId);
        tool.publishNewVersion();
        toolRepository.save(tool);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("TOOL_NAME_REQUIRED", "Tool name is required");
        }
        if (toolRepository.existsByName(name)) {
            throw new BusinessRuleViolationException("TOOL_NAME_UNIQUE",
                    String.format("Tool with name '%s' already exists", name));
        }
    }

    private void validateCapability(String capability) {
        if (capability == null || capability.isBlank()) {
            throw new BusinessRuleViolationException("TOOL_CAPABILITY_REQUIRED", "Tool capability is required");
        }
    }
}
