package com.openlifeops.api.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("mcp")
public class ToolDiscoveryService {

    private final List<McpSyncClient> mcpSyncClients;

    public ToolDiscoveryService(@Qualifier("mcpSyncClients") List<McpSyncClient> mcpSyncClients) {
        this.mcpSyncClients = mcpSyncClients;
    }

    public List<DiscoveredTool> listTools() {
        List<DiscoveredTool> discovered = new ArrayList<>();
        for (McpSyncClient client : mcpSyncClients) {
            String serverName = client.getClientInfo().name();
            McpSchema.ListToolsResult tools = client.listTools();
            for (McpSchema.Tool tool : tools.tools()) {
                discovered.add(new DiscoveredTool(serverName, tool.name(), tool.description()));
            }
        }
        return List.copyOf(discovered);
    }

    public record DiscoveredTool(String serverName, String toolName, String description) {
    }
}
