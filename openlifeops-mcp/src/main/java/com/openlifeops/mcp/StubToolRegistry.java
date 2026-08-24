package com.openlifeops.mcp;

import com.openlifeops.core.domain.Action;

public final class StubToolRegistry implements ToolRegistry {

    @Override
    public ToolInvocationResult invoke(String packId, String objective, Action action) {
        String output = "stub-result:" + action.getToolId() + "@" + action.getTarget();
        return new ToolInvocationResult(output, "executor.stub", 0L);
    }
}
