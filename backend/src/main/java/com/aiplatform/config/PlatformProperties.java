package com.aiplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "platform")
@Getter
@Setter
public class PlatformProperties {

    private WorkspaceProperties workspace = new WorkspaceProperties();
    private GitProperties git = new GitProperties();
    private RagProperties rag = new RagProperties();
    private CodebaseProperties codebase = new CodebaseProperties();
    private MemoryProperties memory = new MemoryProperties();

    @Getter
    @Setter
    public static class WorkspaceProperties {
        private String basePath = "/tmp/ai-platform/workspaces";
    }

    @Getter
    @Setter
    public static class GitProperties {
        private String defaultAuthorName = "AI Platform";
        private String defaultAuthorEmail = "ai@platform.local";
    }

    @Getter
    @Setter
    public static class RagProperties {
        private int chunkSize = 1000;
        private int chunkOverlap = 200;
        private int maxResults = 10;
        private double similarityThreshold = 0.75;
    }

    @Getter
    @Setter
    public static class CodebaseProperties {
        private String parserVersion = "1.0.0";
        private boolean fullIndexOnCreation = true;
    }

    @Getter
    @Setter
    public static class MemoryProperties {
        private int activeRetentionDays = 30;
        private int longTermPromotionThreshold = 5;
    }
}
