package com.aiplatform.domain.tool;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ToolRepository extends BaseRepository<Tool> {
    Optional<Tool> findByName(String name);
    boolean existsByName(String name);
    List<Tool> findByCapability(String capability);
    List<Tool> findByConnectorId(UUID connectorId);
    List<Tool> findByToolType(ToolType toolType);
}
