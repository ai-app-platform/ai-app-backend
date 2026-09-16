package com.aiplatform.domain.connector;

import java.util.Set;
import java.util.UUID;

public record ConnectorCreateCommand(
    String name,
    ConnectorType connectorType,
    String provider,
    String endpoint,
    String repositoryRef,
    UUID projectId,
    String credentialRef,
    AdapterType adapterType,
    String connectionConfiguration,
    String runtimeConfiguration,
    ConnectorPermission permission,
    Set<String> capabilities
) {}
