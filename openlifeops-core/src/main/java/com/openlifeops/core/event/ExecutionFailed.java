package com.openlifeops.core.event;

import java.time.Instant;

public record ExecutionFailed(String taskId, String executionId, String message, Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "ExecutionFailed";
    }
}
