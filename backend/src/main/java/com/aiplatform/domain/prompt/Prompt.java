package com.aiplatform.domain.prompt;

import com.aiplatform.shared.domain.AggregateRoot;
import com.aiplatform.shared.domain.EntityStatus;
import com.aiplatform.shared.domain.VersionInfo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prompts", indexes = {
    @Index(name = "idx_prompts_name", columnList = "name"),
    @Index(name = "idx_prompts_scope", columnList = "scope")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Prompt extends AggregateRoot {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PromptScope scope = PromptScope.PLATFORM;

    @Column(name = "project_id")
    private java.util.UUID projectId;

    @Column(name = "template", nullable = false, columnDefinition = "TEXT")
    private String template;

    @Column(name = "variables", columnDefinition = "jsonb")
    private String variables;

    @Column(name = "model_configuration", columnDefinition = "jsonb")
    private String modelConfiguration;

    @Column(name = "composition_rules", columnDefinition = "jsonb")
    private String compositionRules;

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
        return "PROMPT";
    }

    public boolean isPlatformPrompt() {
        return this.scope == PromptScope.PLATFORM;
    }

    public boolean isProjectPrompt() {
        return this.scope == PromptScope.PROJECT;
    }

    public String render(java.util.Map<String, String> context) {
        String rendered = this.template;
        for (var entry : context.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return rendered;
    }

    public void publishNewVersion() {
        this.versionInfo = this.versionInfo.increment();
    }
}
