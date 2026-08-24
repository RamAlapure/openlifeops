package com.openlifeops.mcp;

import com.openlifeops.core.domain.Action;

import java.util.Map;

public record ToolInvocationResult(
        String output,
        String provenance,
        long durationMs,
        Map<String, String> metadata) {

    public ToolInvocationResult(String output, String provenance, long durationMs) {
        this(output, provenance, durationMs, Map.of());
    }
}
