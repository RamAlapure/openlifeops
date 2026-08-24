package com.openlifeops.core.event;

public sealed interface DomainEvent permits
        TaskCreated,
        PlanCreated,
        ExecutionStarted,
        StepStarted,
        StepCompleted,
        ApprovalRequired,
        ExecutionCheckpointed,
        ExecutionFailed,
        ValidationFailed,
        TaskCompleted {

    String eventType();

    String taskId();

    java.time.Instant occurredAt();
}
