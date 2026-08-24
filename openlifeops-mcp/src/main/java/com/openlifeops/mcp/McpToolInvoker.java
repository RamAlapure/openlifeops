package com.openlifeops.mcp;

import com.openlifeops.core.domain.Action;
import com.openlifeops.core.mcp.McpToolReference;

public interface McpToolInvoker {

    ToolInvocationResult invoke(
            McpToolReference reference, String packId, String objective, Action action);
}
