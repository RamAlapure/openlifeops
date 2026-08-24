package com.openlifeops.mcp;

import com.openlifeops.core.domain.Action;

public interface ToolRegistry {

    ToolInvocationResult invoke(String packId, String objective, Action action);
}
