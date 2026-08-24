package com.openlifeops.core.event;

import java.time.Instant;

public record ExecutionCheckpointed(
        String taskId,
        String executionId,
        String actionId,
        String checkpointType,
        Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "ExecutionCheckpointed";
    }
}
