package com.aiplatform.domain.knowledge;

import com.aiplatform.shared.exception.BusinessRuleViolationException;
import com.aiplatform.shared.exception.EntityNotFoundException;
import com.aiplatform.shared.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeService implements CrudService<KnowledgeEntry, KnowledgeCreateCommand, KnowledgeUpdateCommand> {

    private final KnowledgeRepository knowledgeRepository;

    @Override
    @Transactional
    public KnowledgeEntry create(KnowledgeCreateCommand command) {
        validateCommand(command);

        KnowledgeEntry entry = KnowledgeEntry.builder()
                .title(command.title())
                .content(command.content())
                .category(command.category())
                .scope(command.scope() != null ? command.scope() : KnowledgeScope.PLATFORM)
                .projectId(command.projectId())
                .workflowId(command.workflowId())
                .sourceRef(command.sourceRef())
                .sourceCommitSha(command.sourceCommitSha())
                .filePath(command.filePath())
                .priority(command.priority() != null ? command.priority() : 0)
                .tags(command.tags())
                .versionInfo(com.aiplatform.shared.domain.VersionInfo.initial())
                .build();

        return knowledgeRepository.save(entry);
    }

    @Override
    @Transactional
    public KnowledgeEntry update(UUID id, KnowledgeUpdateCommand command) {
        KnowledgeEntry entry = findByIdOrThrow(id);

        if (command.title() != null) entry.setTitle(command.title());
        if (command.content() != null) entry.setContent(command.content());
        if (command.category() != null) entry.setCategory(command.category());
        if (command.priority() != null) entry.setPriority(command.priority());
        if (command.tags() != null) entry.setTags(command.tags());

        return knowledgeRepository.save(entry);
    }

    @Override
    public Optional<KnowledgeEntry> findById(UUID id) {
        return knowledgeRepository.findById(id);
    }

    @Override
    public List<KnowledgeEntry> findAll(int page, int size) {
        return knowledgeRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        KnowledgeEntry entry = findByIdOrThrow(id);
        entry.setStatus(com.aiplatform.shared.domain.EntityStatus.ARCHIVED);
        knowledgeRepository.save(entry);
    }

    public KnowledgeEntry findByIdOrThrow(UUID id) {
        return knowledgeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("KnowledgeEntry", id));
    }

    /**
     * Resolves knowledge using the hierarchy: Platform → Project → Workflow → Task
     * Higher scope overrides lower scope (explicit and traceable).
     */
    public List<KnowledgeEntry> resolveKnowledge(UUID projectId, UUID workflowId) {
        List<KnowledgeEntry> resolved = new ArrayList<>();

        // Level 1: Platform knowledge
        resolved.addAll(knowledgeRepository.findByScope(KnowledgeScope.PLATFORM));

        // Level 2: Project knowledge
        if (projectId != null) {
            resolved.addAll(knowledgeRepository.findByScopeAndProjectId(KnowledgeScope.PROJECT, projectId));
        }

        // Level 3: Workflow knowledge
        if (workflowId != null) {
            resolved.addAll(knowledgeRepository.findByScope(KnowledgeScope.WORKFLOW));
        }

        return resolved;
    }

    public List<KnowledgeEntry> findPlatformKnowledge() {
        return knowledgeRepository.findByScope(KnowledgeScope.PLATFORM);
    }

    public List<KnowledgeEntry> findProjectKnowledge(UUID projectId) {
        return knowledgeRepository.findByProjectId(projectId);
    }

    private void validateCommand(KnowledgeCreateCommand command) {
        if (command.title() == null || command.title().isBlank()) {
            throw new BusinessRuleViolationException("KNOWLEDGE_TITLE_REQUIRED", "Knowledge title is required");
        }
        if (command.content() == null || command.content().isBlank()) {
            throw new BusinessRuleViolationException("KNOWLEDGE_CONTENT_REQUIRED", "Knowledge content is required");
        }
    }
}
