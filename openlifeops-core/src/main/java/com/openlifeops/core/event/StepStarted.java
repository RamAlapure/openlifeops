package com.openlifeops.core.event;

import java.time.Instant;

public record StepStarted(String taskId, String executionId, String stepId, Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "StepStarted";
    }
}
