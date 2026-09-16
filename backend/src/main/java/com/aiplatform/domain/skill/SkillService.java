package com.aiplatform.domain.skill;

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
public class SkillService implements CrudService<Skill, SkillCreateCommand, SkillUpdateCommand> {

    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public Skill create(SkillCreateCommand command) {
        validateName(command.name());

        Skill skill = Skill.builder()
                .name(command.name())
                .description(command.description())
                .category(command.category())
                .expertiseLevel(command.expertiseLevel() != null ? command.expertiseLevel() : ExpertiseLevel.INTERMEDIATE)
                .configuration(command.configuration())
                .build();

        return skillRepository.save(skill);
    }

    @Override
    @Transactional
    public Skill update(UUID id, SkillUpdateCommand command) {
        Skill skill = findByIdOrThrow(id);

        if (command.description() != null) skill.setDescription(command.description());
        if (command.category() != null) skill.setCategory(command.category());
        if (command.expertiseLevel() != null) skill.setExpertiseLevel(command.expertiseLevel());
        if (command.configuration() != null) skill.setConfiguration(command.configuration());

        return skillRepository.save(skill);
    }

    @Override
    public Optional<Skill> findById(UUID id) {
        return skillRepository.findById(id);
    }

    @Override
    public List<Skill> findAll(int page, int size) {
        return skillRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Skill skill = findByIdOrThrow(id);
        skill.setStatus(com.aiplatform.shared.domain.EntityStatus.ARCHIVED);
        skillRepository.save(skill);
    }

    public Skill findByIdOrThrow(UUID id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill", id));
    }

    public List<Skill> findByCategory(String category) {
        return skillRepository.findByCategory(category);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("SKILL_NAME_REQUIRED", "Skill name is required");
        }
        if (skillRepository.existsByName(name)) {
            throw new BusinessRuleViolationException("SKILL_NAME_UNIQUE",
                    String.format("Skill with name '%s' already exists", name));
        }
    }
}
