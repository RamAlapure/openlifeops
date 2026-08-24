package com.openlifeops.core.api;

import com.openlifeops.core.domain.ApprovalDecision;

import java.util.Objects;

public final class ApprovalRequest {

    private final String actionId;
    private final ApprovalDecision decision;
    private final String decidedBy;
    private final String comment;

    public ApprovalRequest(String actionId, ApprovalDecision decision, String decidedBy, String comment) {
        this.actionId = Objects.requireNonNull(actionId, "actionId");
        this.decision = Objects.requireNonNull(decision, "decision");
        this.decidedBy = decidedBy == null || decidedBy.isBlank() ? "user" : decidedBy;
        this.comment = comment;
    }

    public String getActionId() {
        return actionId;
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
}
