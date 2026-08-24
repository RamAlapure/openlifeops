package com.openlifeops.core.event;

import java.time.Instant;

public record PlanCreated(String taskId, String executionId, String planId, Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "PlanCreated";
    }
}
