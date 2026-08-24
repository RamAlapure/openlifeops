package com.openlifeops.core.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Execution {

    private final String id;
    private final String taskId;
    private final int attemptNumber;
    private ExecutionStatus status;
    private final Instant startedAt;
    private Instant completedAt;
    private String planId;
    private String workflowId;
    private int currentStepIndex;
    private String pendingActionId;
    private String pendingStepId;
    private CheckpointType lastCheckpointType;
    private Instant lastCheckpointAt;
    private final List<Step> steps;
    private final Set<String> executedActionIds;

    public Execution(String id, String taskId, int attemptNumber, ExecutionStatus status, Instant startedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.taskId = Objects.requireNonNull(taskId, "taskId");
        this.attemptNumber = attemptNumber;
        this.status = Objects.requireNonNull(status, "status");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.steps = new ArrayList<>();
        this.executedActionIds = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = Objects.requireNonNull(status, "status");
        if (status == ExecutionStatus.COMPLETED
                || status == ExecutionStatus.FAILED
                || status == ExecutionStatus.CANCELLED) {
            this.completedAt = Instant.now();
        }
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public void setCurrentStepIndex(int currentStepIndex) {
        this.currentStepIndex = currentStepIndex;
    }

    public String getPendingActionId() {
        return pendingActionId;
    }

    public void setPendingActionId(String pendingActionId) {
        this.pendingActionId = pendingActionId;
    }

    public String getPendingStepId() {
        return pendingStepId;
    }

    public void setPendingStepId(String pendingStepId) {
        this.pendingStepId = pendingStepId;
    }

    public CheckpointType getLastCheckpointType() {
        return lastCheckpointType;
    }

    public Instant getLastCheckpointAt() {
        return lastCheckpointAt;
    }

    public void recordCheckpoint(ExecutionCheckpoint checkpoint) {
        this.lastCheckpointType = checkpoint.getCheckpointType();
        this.lastCheckpointAt = checkpoint.getCreatedAt();
        this.currentStepIndex = checkpoint.getStepIndex();
        this.pendingActionId = checkpoint.getActionId();
        this.pendingStepId = checkpoint.getStepId();
    }

    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public void setSteps(List<Step> plannedSteps) {
        steps.clear();
        steps.addAll(plannedSteps);
    }

    public void updateStepStatus(String stepId, StepStatus status) {
        for (Step step : steps) {
            if (step.getId().equals(stepId)) {
                step.setStatus(status);
                return;
            }
        }
    }

    public Set<String> getExecutedActionIds() {
        return Collections.unmodifiableSet(executedActionIds);
    }

    public boolean hasExecutedAction(String actionId) {
        return executedActionIds.contains(actionId);
    }

    public void markActionExecuted(String actionId) {
        executedActionIds.add(Objects.requireNonNull(actionId, "actionId"));
    }

    public void clearPendingApproval() {
        pendingActionId = null;
        pendingStepId = null;
    }
}
