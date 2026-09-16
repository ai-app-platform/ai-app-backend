package com.aiplatform.domain.connector;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "connectors", indexes = {
    @Index(name = "idx_connectors_provider", columnList = "provider"),
    @Index(name = "idx_connectors_type", columnList = "connector_type"),
    @Index(name = "idx_connectors_project", columnList = "project_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Connector extends AggregateRoot {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "connector_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConnectorType connectorType;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "repository_ref")
    private String repositoryRef;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "credential_ref", nullable = false)
    private String credentialRef;

    @Column(name = "adapter_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AdapterType adapterType = AdapterType.REST;

    @Column(name = "connection_configuration", columnDefinition = "jsonb")
    private String connectionConfiguration;

    @Column(name = "runtime_configuration", columnDefinition = "jsonb")
    private String runtimeConfiguration;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    @Builder.Default
    private ConnectorPermission permission = ConnectorPermission.READ_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_state", nullable = false)
    @Builder.Default
    private ConnectionState connectionState = ConnectionState.CREATED;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "connector_capabilities", joinColumns = @JoinColumn(name = "connector_id"))
    @Column(name = "capability")
    @Builder.Default
    private Set<String> capabilities = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "health_check_result", columnDefinition = "jsonb")
    private String healthCheckResult;

    @Override
    public String getAggregateType() {
        return "CONNECTOR";
    }

    public boolean isGitConnector() {
        return this.connectorType == ConnectorType.GIT;
    }

    public boolean isConnected() {
        return this.connectionState == ConnectionState.ACTIVE;
    }

    public void addCapability(String capability) {
        this.capabilities.add(capability);
    }

    public void removeCapability(String capability) {
        this.capabilities.remove(capability);
    }

    public boolean hasCapability(String capability) {
        return this.capabilities.contains(capability);
    }

    public void transitionToConnected() {
        this.connectionState = ConnectionState.ACTIVE;
    }

    public void transitionToDisconnected() {
        this.connectionState = ConnectionState.DISCONNECTED;
    }

    public void transitionToConfigured() {
        this.connectionState = ConnectionState.CONFIGURED;
    }

    public void validatePermission(ConnectorPermission requiredPermission) {
        if (this.permission == ConnectorPermission.READ_ONLY && requiredPermission == ConnectorPermission.READ_WRITE) {
            throw new com.aiplatform.shared.exception.BusinessRuleViolationException(
                "CONNECTOR_PERMISSION_DENIED",
                "Connector does not have write permission"
            );
        }
    }
}
