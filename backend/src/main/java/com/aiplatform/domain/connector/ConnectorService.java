package com.aiplatform.domain.connector;

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
public class ConnectorService implements CrudService<Connector, ConnectorCreateCommand, ConnectorUpdateCommand> {

    private final ConnectorRepository connectorRepository;

    @Override
    @Transactional
    public Connector create(ConnectorCreateCommand command) {
        validateCommand(command);

        Connector.ConnectorBuilder builder = Connector.builder()
                .name(command.name())
                .connectorType(command.connectorType())
                .provider(command.provider())
                .endpoint(command.endpoint())
                .repositoryRef(command.repositoryRef())
                .projectId(command.projectId())
                .credentialRef(command.credentialRef())
                .adapterType(command.adapterType() != null ? command.adapterType() : AdapterType.REST)
                .connectionConfiguration(command.connectionConfiguration())
                .runtimeConfiguration(command.runtimeConfiguration())
                .permission(command.permission() != null ? command.permission() : ConnectorPermission.READ_ONLY)
                .connectionState(ConnectionState.CREATED);

        if (command.capabilities() != null) {
            builder.capabilities(command.capabilities());
        }

        return connectorRepository.save(builder.build());
    }

    @Override
    @Transactional
    public Connector update(UUID id, ConnectorUpdateCommand command) {
        Connector connector = findByIdOrThrow(id);

        if (command.endpoint() != null) connector.setEndpoint(command.endpoint());
        if (command.repositoryRef() != null) connector.setRepositoryRef(command.repositoryRef());
        if (command.credentialRef() != null) connector.setCredentialRef(command.credentialRef());
        if (command.adapterType() != null) connector.setAdapterType(command.adapterType());
        if (command.connectionConfiguration() != null) connector.setConnectionConfiguration(command.connectionConfiguration());
        if (command.runtimeConfiguration() != null) connector.setRuntimeConfiguration(command.runtimeConfiguration());
        if (command.permission() != null) connector.setPermission(command.permission());
        if (command.capabilities() != null) connector.setCapabilities(command.capabilities());

        return connectorRepository.save(connector);
    }

    @Override
    public Optional<Connector> findById(UUID id) {
        return connectorRepository.findById(id);
    }

    @Override
    public List<Connector> findAll(int page, int size) {
        return connectorRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Connector connector = findByIdOrThrow(id);
        connector.setStatus(com.aiplatform.shared.domain.EntityStatus.ARCHIVED);
        connector.transitionToDisconnected();
        connectorRepository.save(connector);
    }

    public Connector findByIdOrThrow(UUID id) {
        return connectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Connector", id));
    }

    public List<Connector> findByProject(UUID projectId) {
        return connectorRepository.findByProjectId(projectId);
    }

    public List<Connector> findByType(ConnectorType type) {
        return connectorRepository.findByConnectorType(type);
    }

    @Transactional
    public Connector activate(UUID id) {
        Connector connector = findByIdOrThrow(id);
        connector.transitionToConnected();
        return connectorRepository.save(connector);
    }

    @Transactional
    public Connector deactivate(UUID id) {
        Connector connector = findByIdOrThrow(id);
        connector.transitionToDisconnected();
        return connectorRepository.save(connector);
    }

    public void validatePermission(UUID connectorId, ConnectorPermission requiredPermission) {
        Connector connector = findByIdOrThrow(connectorId);
        connector.validatePermission(requiredPermission);
    }

    private void validateCommand(ConnectorCreateCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new BusinessRuleViolationException("CONNECTOR_NAME_REQUIRED", "Connector name is required");
        }
        if (command.credentialRef() == null || command.credentialRef().isBlank()) {
            throw new BusinessRuleViolationException("CONNECTOR_CREDENTIAL_REQUIRED",
                    "Credential reference is required - credentials must not be stored in plaintext");
        }
        if (command.connectorType() == null) {
            throw new BusinessRuleViolationException("CONNECTOR_TYPE_REQUIRED", "Connector type is required");
        }
        if (command.provider() == null || command.provider().isBlank()) {
            throw new BusinessRuleViolationException("CONNECTOR_PROVIDER_REQUIRED", "Connector provider is required");
        }
    }
}
