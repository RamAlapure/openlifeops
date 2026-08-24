package com.openlifeops.core.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApprovalTest {

    @Test
    void decisionUpdatesApprovalStatus() {
        Approval approval = new Approval(
                "approval-1",
                "task-1",
                "execution-1",
                "action-1",
                ApprovalStatus.PENDING,
                Instant.now());

        approval.decide(ApprovalDecision.APPROVED, "user", "approved", Instant.now());

        assertEquals(ApprovalStatus.DECIDED, approval.getStatus());
        assertEquals(ApprovalDecision.APPROVED, approval.getDecision());
    }
}
