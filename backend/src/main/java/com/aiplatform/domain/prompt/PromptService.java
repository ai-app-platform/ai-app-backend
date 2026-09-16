package com.aiplatform.domain.prompt;

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
public class PromptService implements CrudService<Prompt, PromptCreateCommand, PromptUpdateCommand> {

    private final PromptRepository promptRepository;

    @Override
    @Transactional
    public Prompt create(PromptCreateCommand command) {
        validateCommand(command);

        Prompt prompt = Prompt.builder()
                .name(command.name())
                .description(command.description())
                .scope(command.scope() != null ? command.scope() : PromptScope.PLATFORM)
                .projectId(command.projectId())
                .template(command.template())
                .variables(command.variables())
                .modelConfiguration(command.modelConfiguration())
                .compositionRules(command.compositionRules())
                .versionInfo(com.aiplatform.shared.domain.VersionInfo.initial())
                .build();

        return promptRepository.save(prompt);
    }

    @Override
    @Transactional
    public Prompt update(UUID id, PromptUpdateCommand command) {
        Prompt prompt = findByIdOrThrow(id);

        if (command.description() != null) prompt.setDescription(command.description());
        if (command.template() != null) prompt.setTemplate(command.template());
        if (command.variables() != null) prompt.setVariables(command.variables());
        if (command.modelConfiguration() != null) prompt.setModelConfiguration(command.modelConfiguration());
        if (command.compositionRules() != null) prompt.setCompositionRules(command.compositionRules());

        return promptRepository.save(prompt);
    }

    @Override
    public Optional<Prompt> findById(UUID id) {
        return promptRepository.findById(id);
    }

    @Override
    public List<Prompt> findAll(int page, int size) {
        return promptRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Prompt prompt = findByIdOrThrow(id);
        prompt.setStatus(com.aiplatform.shared.domain.EntityStatus.ARCHIVED);
        promptRepository.save(prompt);
    }

    public Prompt findByIdOrThrow(UUID id) {
        return promptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prompt", id));
    }

    public List<Prompt> findByScope(PromptScope scope) {
        return promptRepository.findByScope(scope);
    }

    public List<Prompt> findProjectPrompts(UUID projectId) {
        return promptRepository.findByProjectId(projectId);
    }

    @Transactional
    public void publishNewVersion(UUID promptId) {
        Prompt prompt = findByIdOrThrow(promptId);
        prompt.publishNewVersion();
        promptRepository.save(prompt);
    }

    private void validateCommand(PromptCreateCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new BusinessRuleViolationException("PROMPT_NAME_REQUIRED", "Prompt name is required");
        }
        if (command.template() == null || command.template().isBlank()) {
            throw new BusinessRuleViolationException("PROMPT_TEMPLATE_REQUIRED", "Prompt template is required");
        }
    }
}
