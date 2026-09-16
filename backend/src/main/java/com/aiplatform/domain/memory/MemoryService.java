package com.aiplatform.domain.memory;

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
public class MemoryService implements CrudService<Memory, MemoryCreateCommand, MemoryUpdateCommand> {

    private final MemoryRepository memoryRepository;

    @Override
    @Transactional
    public Memory create(MemoryCreateCommand command) {
        validateCommand(command);

        Memory memory = Memory.builder()
                .projectId(command.projectId())
                .taskId(command.taskId())
                .memoryType(command.memoryType())
                .title(command.title())
                .content(command.content())
                .filePath(command.filePath())
                .metadata(command.metadata())
                .build();

        return memoryRepository.save(memory);
    }

    @Override
    @Transactional
    public Memory update(UUID id, MemoryUpdateCommand command) {
        Memory memory = findByIdOrThrow(id);

        if (command.title() != null) memory.setTitle(command.title());
        if (command.content() != null) memory.setContent(command.content());
        if (command.summary() != null) memory.setSummary(command.summary());
        if (command.metadata() != null) memory.setMetadata(command.metadata());

        return memoryRepository.save(memory);
    }

    @Override
    public Optional<Memory> findById(UUID id) {
        return memoryRepository.findById(id);
    }

    @Override
    public List<Memory> findAll(int page, int size) {
        return memoryRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        memoryRepository.deleteById(id);
    }

    public Memory findByIdOrThrow(UUID id) {
        return memoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Memory", id));
    }

    public List<Memory> findActiveMemories(UUID projectId) {
        return memoryRepository.findByProjectIdAndMemoryType(projectId, MemoryType.ACTIVE);
    }

    public List<Memory> findLongTermMemories(UUID projectId) {
        return memoryRepository.findByProjectIdAndPromoted(projectId, true);
    }

    public List<Memory> findTaskMemories(UUID taskId) {
        return memoryRepository.findByTaskId(taskId);
    }

    @Transactional
    public Memory promoteToLongTerm(UUID memoryId, String summary) {
        Memory memory = findByIdOrThrow(memoryId);
        memory.promoteToLongTerm(summary);
        return memoryRepository.save(memory);
    }

    @Transactional
    public Memory recordTaskState(UUID projectId, UUID taskId, String content) {
        Memory memory = Memory.builder()
                .projectId(projectId)
                .taskId(taskId)
                .memoryType(MemoryType.TASK_STATE)
                .title("Task State")
                .content(content)
                .build();
        return memoryRepository.save(memory);
    }

    @Transactional
    public Memory recordDiscovery(UUID projectId, UUID taskId, String title, String content) {
        Memory memory = Memory.builder()
                .projectId(projectId)
                .taskId(taskId)
                .memoryType(MemoryType.DISCOVERY)
                .title(title)
                .content(content)
                .build();
        return memoryRepository.save(memory);
    }

    @Transactional
    public Memory recordDecision(UUID projectId, UUID taskId, String title, String content) {
        Memory memory = Memory.builder()
                .projectId(projectId)
                .taskId(taskId)
                .memoryType(MemoryType.DECISION)
                .title(title)
                .content(content)
                .build();
        return memoryRepository.save(memory);
    }

    private void validateCommand(MemoryCreateCommand command) {
        if (command.projectId() == null) {
            throw new BusinessRuleViolationException("MEMORY_PROJECT_REQUIRED", "Project ID is required for memory");
        }
        if (command.content() == null || command.content().isBlank()) {
            throw new BusinessRuleViolationException("MEMORY_CONTENT_REQUIRED", "Memory content is required");
        }
        if (command.memoryType() == null) {
            throw new BusinessRuleViolationException("MEMORY_TYPE_REQUIRED", "Memory type is required");
        }
    }
}
