package com.openlifeops.api.mcp;

import com.openlifeops.core.domain.Action;
import com.openlifeops.core.mcp.McpToolReference;
import com.openlifeops.mcp.McpToolException;
import com.openlifeops.mcp.McpToolInvoker;
import com.openlifeops.mcp.ToolInvocationResult;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import com.openlifeops.api.config.OpenLifeOpsMcpProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("mcp")
public class SpringMcpToolInvoker implements McpToolInvoker {

    private final Map<String, McpSyncClient> clientsByServerId;

    public SpringMcpToolInvoker(
            @Qualifier("mcpSyncClients") List<McpSyncClient> mcpSyncClients,
            OpenLifeOpsMcpProperties properties) {
        this.clientsByServerId = mapClients(mcpSyncClients, properties);
    }

    @Override
    public ToolInvocationResult invoke(
            McpToolReference reference, String packId, String objective, Action action) {
        McpSyncClient client = clientsByServerId.get(reference.serverId());
        if (client == null) {
            throw new McpToolException("No MCP client configured for server '" + reference.serverId() + "'");
        }

        long startedAt = System.nanoTime();
        try {
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("target", action.getTarget());
            arguments.put("toolId", action.getToolId());
            arguments.put("packId", packId);
            arguments.put("objective", objective);
            if (!action.getParameters().isEmpty()) {
                arguments.putAll(action.getParameters());
            }

            McpSchema.CallToolResult result = client.callTool(
                    new McpSchema.CallToolRequest(reference.mcpToolName(), arguments));
            if (result.isError() != null && result.isError()) {
                throw new McpToolException("MCP tool '" + reference.mcpToolName() + "' returned an error");
            }

            String output = extractText(result);
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;
            return new ToolInvocationResult(output, "mcp:" + reference.serverId(), durationMs);
        } catch (McpToolException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new McpToolException(
                    "Failed to invoke MCP tool '" + reference.mcpToolName() + "' on server '"
                            + reference.serverId() + "'",
                    exception);
        }
    }

    private static Map<String, McpSyncClient> mapClients(
            List<McpSyncClient> mcpSyncClients,
            OpenLifeOpsMcpProperties properties) {
        Map<String, McpSyncClient> mapped = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.getServerConnections().entrySet()) {
            String serverId = entry.getKey();
            String connectionName = entry.getValue();
            McpSyncClient client = mcpSyncClients.stream()
                    .filter(candidate -> connectionName.equals(candidate.getClientInfo().name()))
                    .findFirst()
                    .orElse(null);
            if (client == null && mcpSyncClients.size() == 1) {
                client = mcpSyncClients.getFirst();
            }
            if (client != null) {
                mapped.put(serverId, client);
            }
        }
        return Map.copyOf(mapped);
    }

    private static String extractText(McpSchema.CallToolResult result) {
        if (result.content() == null || result.content().isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (McpSchema.Content content : result.content()) {
            if (content instanceof McpSchema.TextContent textContent) {
                if (!builder.isEmpty()) {
                    builder.append('\n');
                }
                builder.append(textContent.text());
            }
        }
        return builder.toString();
    }
}
