package com.aiplatform.domain.connector;

import java.util.Set;

public record ConnectorUpdateCommand(
    String endpoint,
    String repositoryRef,
    String credentialRef,
    AdapterType adapterType,
    String connectionConfiguration,
    String runtimeConfiguration,
    ConnectorPermission permission,
    Set<String> capabilities
) {}
