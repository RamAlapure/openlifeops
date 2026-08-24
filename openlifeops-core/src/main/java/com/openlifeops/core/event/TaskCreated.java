package com.openlifeops.core.event;

import java.time.Instant;

public record TaskCreated(String taskId, String packId, Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "TaskCreated";
    }
}
