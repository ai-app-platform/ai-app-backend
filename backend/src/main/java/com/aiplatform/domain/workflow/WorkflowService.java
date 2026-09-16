package com.aiplatform.domain.workflow;

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
public class WorkflowService implements CrudService<Workflow, WorkflowCreateCommand, WorkflowUpdateCommand> {

    private final WorkflowRepository workflowRepository;

    @Override
    @Transactional
    public Workflow create(WorkflowCreateCommand command) {
        validateCommand(command);

        Workflow workflow = Workflow.builder()
                .name(command.name())
                .description(command.description())
                .scope(command.scope() != null ? command.scope() : WorkflowScope.PLATFORM)
                .projectId(command.projectId())
                .definition(command.definition())
                .planningEnabled(command.planningEnabled())
                .agentSelectionStrategy(command.agentSelectionStrategy() != null ? command.agentSelectionStrategy() : "dynamic")
                .allowParallel(command.allowParallel())
                .validationRequired(command.validationRequired())
                .approvalRequired(command.approvalRequired())
                .steps(command.steps())
                .retryPolicy(command.retryPolicy())
                .failureHandling(command.failureHandling())
                .versionInfo(com.aiplatform.shared.domain.VersionInfo.initial())
                .build();

        return workflowRepository.save(workflow);
    }

    @Override
    @Transactional
    public Workflow update(UUID id, WorkflowUpdateCommand command) {
        Workflow workflow = findByIdOrThrow(id);

        if (command.description() != null) workflow.setDescription(command.description());
        if (command.definition() != null) workflow.setDefinition(command.definition());
        if (command.planningEnabled() != null) workflow.setPlanningEnabled(command.planningEnabled());
        if (command.agentSelectionStrategy() != null) workflow.setAgentSelectionStrategy(command.agentSelectionStrategy());
        if (command.allowParallel() != null) workflow.setAllowParallel(command.allowParallel());
        if (command.validationRequired() != null) workflow.setValidationRequired(command.validationRequired());
        if (command.approvalRequired() != null) workflow.setApprovalRequired(command.approvalRequired());
        if (command.steps() != null) workflow.setSteps(command.steps());
        if (command.retryPolicy() != null) workflow.setRetryPolicy(command.retryPolicy());
        if (command.failureHandling() != null) workflow.setFailureHandling(command.failureHandling());

        return workflowRepository.save(workflow);
    }

    @Override
    public Optional<Workflow> findById(UUID id) {
        return workflowRepository.findById(id);
    }

    @Override
    public List<Workflow> findAll(int page, int size) {
        return workflowRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Workflow workflow = findByIdOrThrow(id);
        workflow.setStatus(com.aiplatform.shared.domain.EntityStatus.ARCHIVED);
        workflowRepository.save(workflow);
    }

    public Workflow findByIdOrThrow(UUID id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow", id));
    }

    public List<Workflow> findPlatformWorkflows() {
        return workflowRepository.findByScope(WorkflowScope.PLATFORM);
    }

    public List<Workflow> findProjectWorkflows(UUID projectId) {
        return workflowRepository.findByProjectId(projectId);
    }

    @Transactional
    public void publishNewVersion(UUID workflowId) {
        Workflow workflow = findByIdOrThrow(workflowId);
        workflow.publishNewVersion();
        workflowRepository.save(workflow);
    }

    private void validateCommand(WorkflowCreateCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new BusinessRuleViolationException("WORKFLOW_NAME_REQUIRED", "Workflow name is required");
        }
        if (command.definition() == null || command.definition().isBlank()) {
            throw new BusinessRuleViolationException("WORKFLOW_DEFINITION_REQUIRED", "Workflow definition is required");
        }
    }
}
