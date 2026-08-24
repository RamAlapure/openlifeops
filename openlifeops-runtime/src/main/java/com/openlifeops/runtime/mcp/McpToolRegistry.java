package com.openlifeops.runtime.mcp;

import com.openlifeops.core.domain.Action;
import com.openlifeops.mcp.McpToolInvoker;
import com.openlifeops.mcp.ToolInvocationResult;
import com.openlifeops.mcp.ToolRegistry;

public final class McpToolRegistry implements ToolRegistry {

    private final PackToolResolver packToolResolver;
    private final McpToolInvoker mcpToolInvoker;

    public McpToolRegistry(PackToolResolver packToolResolver, McpToolInvoker mcpToolInvoker) {
        this.packToolResolver = packToolResolver;
        this.mcpToolInvoker = mcpToolInvoker;
    }

    @Override
    public ToolInvocationResult invoke(String packId, String objective, Action action) {
        return mcpToolInvoker.invoke(packToolResolver.resolve(packId, action.getToolId()), packId, objective, action);
    }
}
