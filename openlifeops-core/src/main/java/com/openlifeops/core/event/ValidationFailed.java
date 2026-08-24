package com.openlifeops.core.event;

import java.time.Instant;

public record ValidationFailed(String taskId, String executionId, String stepId, String message, Instant occurredAt)
        implements DomainEvent {

    @Override
    public String eventType() {
        return "ValidationFailed";
    }
}
