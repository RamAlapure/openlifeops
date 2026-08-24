package com.openlifeops.core.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Task {

    private final String id;
    private final String objective;
    private final String packId;
    private TaskStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<String> executionIds;

    public Task(String id, String objective, String packId, TaskStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.objective = Objects.requireNonNull(objective, "objective");
        this.packId = Objects.requireNonNull(packId, "packId");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = createdAt;
        this.executionIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getObjective() {
        return objective;
    }

    public String getPackId() {
        return packId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "status");
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<String> getExecutionIds() {
        return Collections.unmodifiableList(executionIds);
    }

    public void addExecutionId(String executionId) {
        executionIds.add(Objects.requireNonNull(executionId, "executionId"));
        updatedAt = Instant.now();
    }
}
