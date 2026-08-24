package com.openlifeops.core.mcp;

import java.util.Objects;

public record McpToolReference(String serverId, String mcpToolName) {

    public McpToolReference {
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(mcpToolName, "mcpToolName");
    }
}
