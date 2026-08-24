package com.openlifeops.core.domain;

import java.time.Instant;
import java.util.Objects;

public final class ExecutionCheckpoint {

    private final String executionId;
    private final String planId;
    private final int stepIndex;
    private final String stepId;
    private final String actionId;
    private final CheckpointType checkpointType;
    private final Instant createdAt;

    public ExecutionCheckpoint(
            String executionId,
            String planId,
            int stepIndex,
            String stepId,
            String actionId,
            CheckpointType checkpointType,
            Instant createdAt) {
        this.executionId = Objects.requireNonNull(executionId, "executionId");
        this.planId = Objects.requireNonNull(planId, "planId");
        this.stepIndex = stepIndex;
        this.stepId = Objects.requireNonNull(stepId, "stepId");
        this.actionId = Objects.requireNonNull(actionId, "actionId");
        this.checkpointType = Objects.requireNonNull(checkpointType, "checkpointType");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getPlanId() {
        return planId;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public String getStepId() {
        return stepId;
    }

    public String getActionId() {
        return actionId;
    }

    public CheckpointType getCheckpointType() {
        return checkpointType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
