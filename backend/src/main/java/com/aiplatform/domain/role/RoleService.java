package com.aiplatform.domain.role;

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
public class RoleService implements CrudService<Role, RoleCreateCommand, RoleUpdateCommand> {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Role create(RoleCreateCommand command) {
        validateName(command.name());

        Role role = Role.builder()
                .name(command.name())
                .description(command.description())
                .responsibility(command.responsibility())
                .constraints(command.constraints())
                .promptPolicyRef(command.promptPolicyRef())
                .skillPolicyRef(command.skillPolicyRef())
                .toolPolicyRef(command.toolPolicyRef())
                .build();

        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role update(UUID id, RoleUpdateCommand command) {
        Role role = findByIdOrThrow(id);

        if (command.description() != null) role.setDescription(command.description());
        if (command.responsibility() != null) role.setResponsibility(command.responsibility());
        if (command.constraints() != null) role.setConstraints(command.constraints());
        if (command.promptPolicyRef() != null) role.setPromptPolicyRef(command.promptPolicyRef());
        if (command.skillPolicyRef() != null) role.setSkillPolicyRef(command.skillPolicyRef());
        if (command.toolPolicyRef() != null) role.setToolPolicyRef(command.toolPolicyRef());

        return roleRepository.save(role);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return roleRepository.findById(id);
    }

    @Override
    public List<Role> findAll(int page, int size) {
        return roleRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Role role = findByIdOrThrow(id);
        role.setStatus(com.aiplatform.shared.domain.EntityStatus.ARCHIVED);
        roleRepository.save(role);
    }

    public Role findByIdOrThrow(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role", id));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("ROLE_NAME_REQUIRED", "Role name is required");
        }
        if (roleRepository.existsByName(name)) {
            throw new BusinessRuleViolationException("ROLE_NAME_UNIQUE",
                    String.format("Role with name '%s' already exists", name));
        }
    }
}
