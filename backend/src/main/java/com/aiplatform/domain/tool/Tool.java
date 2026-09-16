package com.aiplatform.domain.tool;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import com.aiplatform.shared.domain.VersionInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tools", indexes = {
    @Index(name = "idx_tools_name", columnList = "name"),
    @Index(name = "idx_tools_type", columnList = "tool_type"),
    @Index(name = "idx_tools_capability", columnList = "capability")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tool extends AggregateRoot {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tool_type", nullable = false)
    private ToolType toolType;

    @Column(name = "capability", nullable = false)
    private String capability;

    @Column(name = "input_schema", columnDefinition = "jsonb")
    private String inputSchema;

    @Column(name = "output_schema", columnDefinition = "jsonb")
    private String outputSchema;

    @Column(name = "permission_level")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PermissionLevel permissionLevel = PermissionLevel.STANDARD;

    @Column(name = "connector_id")
    private java.util.UUID connectorId;

    @Column(name = "execution_policy", columnDefinition = "jsonb")
    private String executionPolicy;

    @Column(name = "testing_configuration", columnDefinition = "jsonb")
    private String testingConfiguration;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "semanticVersion", column = @Column(name = "version_semantic")),
        @AttributeOverride(name = "latest", column = @Column(name = "version_latest")),
        @AttributeOverride(name = "versionSequence", column = @Column(name = "version_sequence"))
    })
    private VersionInfo versionInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Override
    public String getAggregateType() {
        return "TOOL";
    }

    public boolean requiresConnector() {
        return this.connectorId != null;
    }

    public void publishNewVersion() {
        this.versionInfo = this.versionInfo.increment();
    }
}
