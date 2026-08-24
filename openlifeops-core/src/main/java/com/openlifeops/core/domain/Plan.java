package com.openlifeops.core.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Frozen workflow snapshot for one execution attempt.
 */
public final class Plan {

    private final String id;
    private final String taskId;
    private final String executionId;
    private final String workflowId;
    private final String workflowVersion;
    private final Instant createdAt;
    private final List<Step> steps;

    public Plan(
            String id,
            String taskId,
            String executionId,
            String workflowId,
            String workflowVersion,
            Instant createdAt,
            List<Step> steps) {
        this.id = Objects.requireNonNull(id, "id");
        this.taskId = Objects.requireNonNull(taskId, "taskId");
        this.executionId = Objects.requireNonNull(executionId, "executionId");
        this.workflowId = Objects.requireNonNull(workflowId, "workflowId");
        this.workflowVersion = Objects.requireNonNull(workflowVersion, "workflowVersion");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.steps = List.copyOf(steps);
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

    public String getWorkflowId() {
        return workflowId;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public int stepCount() {
        return steps.size();
    }
}
