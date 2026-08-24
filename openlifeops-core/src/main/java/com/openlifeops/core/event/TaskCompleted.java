package com.openlifeops.core.event;

import java.time.Instant;

public record TaskCompleted(String taskId, String executionId, Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "TaskCompleted";
    }
}
