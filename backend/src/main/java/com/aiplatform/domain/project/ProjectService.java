package com.aiplatform.domain.project;

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
public class ProjectService implements CrudService<Project, ProjectCreateCommand, ProjectUpdateCommand> {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public Project create(ProjectCreateCommand command) {
        validateProjectName(command.name());

        Project project = Project.builder()
                .name(command.name())
                .description(command.description())
                .repositoryUrl(command.repositoryUrl())
                .defaultBranch(command.defaultBranch() != null ? command.defaultBranch() : "main")
                .configuration(command.configuration())
                .build();

        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project update(UUID id, ProjectUpdateCommand command) {
        Project project = findByIdOrThrow(id);

        if (command.name() != null && !command.name().equals(project.getName())) {
            validateProjectName(command.name());
            project.setName(command.name());
        }
        if (command.description() != null) project.setDescription(command.description());
        if (command.repositoryUrl() != null) project.setRepositoryUrl(command.repositoryUrl());
        if (command.defaultBranch() != null) project.setDefaultBranch(command.defaultBranch());
        if (command.configuration() != null) project.setConfiguration(command.configuration());

        return projectRepository.save(project);
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return projectRepository.findById(id);
    }

    @Override
    public List<Project> findAll(int page, int size) {
        return projectRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Project project = findByIdOrThrow(id);
        project.archive();
        projectRepository.save(project);
    }

    public Project findByIdOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project", id));
    }

    public Page<Project> findActiveProjects(int page, int size) {
        return projectRepository.findByStatus(
                com.aiplatform.shared.domain.EntityStatus.ACTIVE,
                PageRequest.of(page, size)
        );
    }

    private void validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("PROJECT_NAME_REQUIRED", "Project name is required");
        }
        if (projectRepository.existsByName(name)) {
            throw new BusinessRuleViolationException("PROJECT_NAME_UNIQUE",
                    String.format("Project with name '%s' already exists", name));
        }
    }
}
