package com.openlifeops.core.domain;

import java.util.Objects;

public final class Step {

    private final String id;
    private final String stepKey;
    private final String name;
    private final int order;
    private final Action action;
    private StepStatus status;

    public Step(String id, String stepKey, String name, int order, Action action, StepStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.stepKey = Objects.requireNonNull(stepKey, "stepKey");
        this.name = Objects.requireNonNull(name, "name");
        this.order = order;
        this.action = Objects.requireNonNull(action, "action");
        this.status = Objects.requireNonNull(status, "status");
    }

    public String getId() {
        return id;
    }

    public String getStepKey() {
        return stepKey;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public Action getAction() {
        return action;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public boolean isCompleted() {
        return status == StepStatus.COMPLETED;
    }
}
