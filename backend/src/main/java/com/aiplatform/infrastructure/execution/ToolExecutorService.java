package com.aiplatform.infrastructure.execution;

import com.aiplatform.domain.connector.Connector;
import com.aiplatform.domain.connector.ConnectorService;
import com.aiplatform.domain.tool.Tool;
import com.aiplatform.domain.tool.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Tool Executor is the boundary between Agent Tool Calling and actual execution.
 * 
 * Flow: Agent → Tool Definition → Tool Executor → Connector Resolution → Permission Check → Adapter → External System
 * 
 * Agent never directly accesses credentials, bypasses connectors, or calls MCP clients.
 */
@Service
@RequiredArgsConstructor
public class ToolExecutorService {

    private final ToolService toolService;
    private final ConnectorService connectorService;

    /**
     * Executes a tool call with proper permission checking and connector resolution.
     */
    public ToolExecutionResult execute(UUID toolId, Map<String, Object> input, String executedBy) {
        // 1. Resolve tool definition
        Tool tool = toolService.findByIdOrThrow(toolId);

        // 2. Check if tool requires a connector
        if (tool.requiresConnector()) {
            // 3. Resolve connector
            Connector connector = connectorService.findByIdOrThrow(tool.getConnectorId());

            // 4. Permission check
            connector.validatePermission(
                    mapToolPermissionToConnectorPermission(tool.getPermissionLevel()));

            // 5. Execute through adapter
            return executeThroughAdapter(connector, tool, input);
        }

        // Internal tool execution
        return executeInternal(tool, input);
    }

    private ToolExecutionResult executeThroughAdapter(Connector connector, Tool tool,
                                                        Map<String, Object> input) {
        // In a real implementation, this would:
        // 1. Select the appropriate adapter based on connector.adapterType
        // 2. Resolve credentials from secret management (not from connector directly)
        // 3. Execute the operation through the adapter
        // 4. Map the response to the tool's output schema

        return ToolExecutionResult.builder()
                .success(true)
                .toolId(tool.getId())
                .toolName(tool.getName())
                .connectorId(connector.getId())
                .adapterType(connector.getAdapterType().name())
                .build();
    }

    private ToolExecutionResult executeInternal(Tool tool, Map<String, Object> input) {
        // Internal tool execution (e.g., file operations, code analysis)
        return ToolExecutionResult.builder()
                .success(true)
                .toolId(tool.getId())
                .toolName(tool.getName())
                .build();
    }

    private com.aiplatform.domain.connector.ConnectorPermission mapToolPermissionToConnectorPermission(
            com.aiplatform.domain.tool.PermissionLevel level) {
        return switch (level) {
            case READ_ONLY -> com.aiplatform.domain.connector.ConnectorPermission.READ_ONLY;
            case STANDARD, ELEVATED, ADMIN -> com.aiplatform.domain.connector.ConnectorPermission.READ_WRITE;
        };
    }

    @lombok.Builder
    @lombok.Getter
    public static class ToolExecutionResult {
        private boolean success;
        private UUID toolId;
        private String toolName;
        private UUID connectorId;
        private String adapterType;
        private Object output;
        private String error;
    }
}
