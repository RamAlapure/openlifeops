package com.openlifeops.core.domain;

import java.time.Instant;
import java.util.Objects;

public final class Observation {

    private final String id;
    private final String taskId;
    private final String executionId;
    private final String actionId;
    private final String output;
    private final Instant timestamp;

    public Observation(
            String id,
            String taskId,
            String executionId,
            String actionId,
            String output,
            Instant timestamp) {
        this.id = Objects.requireNonNull(id, "id");
        this.taskId = Objects.requireNonNull(taskId, "taskId");
        this.executionId = Objects.requireNonNull(executionId, "executionId");
        this.actionId = Objects.requireNonNull(actionId, "actionId");
        this.output = Objects.requireNonNull(output, "output");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    }

    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getActionId() {
        return actionId;
    }

    public String getOutput() {
        return output;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
