package com.aiplatform.domain.connector;

import com.aiplatform.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectorRepository extends BaseRepository<Connector> {
    List<Connector> findByProjectId(UUID projectId);
    List<Connector> findByConnectorType(ConnectorType connectorType);
    List<Connector> findByProvider(String provider);
    Optional<Connector> findByNameAndProjectId(String name, UUID projectId);
}
