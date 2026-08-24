package com.openlifeops.runtime.mcp;

import com.openlifeops.core.mcp.McpToolReference;
import com.openlifeops.core.pack.OpenLifeOpsPack;
import com.openlifeops.mcp.McpToolException;
import com.openlifeops.runtime.pack.PackRegistry;

public final class PackToolResolver {

    private final PackRegistry packRegistry;

    public PackToolResolver(PackRegistry packRegistry) {
        this.packRegistry = packRegistry;
    }

    public McpToolReference resolve(String packId, String toolId) {
        OpenLifeOpsPack pack = packRegistry.require(packId);
        McpToolReference reference = pack.mcpToolBindings().get(toolId);
        if (reference == null) {
            throw new McpToolException("No MCP binding for toolId '" + toolId + "' in pack '" + packId + "'");
        }
        return reference;
    }
}
