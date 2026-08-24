package com.openlifeops.core.event;

import java.time.Instant;

public record StepCompleted(String taskId, String executionId, String stepId, Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "StepCompleted";
    }
}
