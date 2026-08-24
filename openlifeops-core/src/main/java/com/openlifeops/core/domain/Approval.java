package com.openlifeops.core.domain;

import java.time.Instant;
import java.util.Objects;

public final class Approval {

    private final String id;
    private final String taskId;
    private final String executionId;
    private final String actionId;
    private ApprovalStatus status;
    private final Instant requestedAt;
    private ApprovalDecision decision;
    private String decidedBy;
    private String comment;
    private Instant decidedAt;

    public Approval(
            String id,
            String taskId,
            String executionId,
            String actionId,
            ApprovalStatus status,
            Instant requestedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.taskId = Objects.requireNonNull(taskId, "taskId");
        this.executionId = Objects.requireNonNull(executionId, "executionId");
        this.actionId = Objects.requireNonNull(actionId, "actionId");
        this.status = Objects.requireNonNull(status, "status");
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt");
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

    public ApprovalStatus getStatus() {
        return status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public ApprovalDecision getDecision() {
        return decision;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public String getComment() {
        return comment;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void decide(ApprovalDecision decision, String decidedBy, String comment, Instant decidedAt) {
        this.decision = Objects.requireNonNull(decision, "decision");
        this.decidedBy = decidedBy == null || decidedBy.isBlank() ? "user" : decidedBy;
        this.comment = comment;
        this.decidedAt = Objects.requireNonNull(decidedAt, "decidedAt");
        this.status = ApprovalStatus.DECIDED;
    }

    public boolean isDecided() {
        return decision != null;
    }
}
