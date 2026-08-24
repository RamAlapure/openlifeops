package com.openlifeops.core.event;

import java.time.Instant;

public record ApprovalRequired(String taskId, String executionId, String actionId, Instant occurredAt) implements DomainEvent {

    @Override
    public String eventType() {
        return "ApprovalRequired";
    }
}
