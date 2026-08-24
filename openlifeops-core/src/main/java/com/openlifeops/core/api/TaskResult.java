package com.openlifeops.core.api;

import com.openlifeops.core.domain.TaskStatus;

import java.util.Objects;

public final class TaskResult {

    private final String taskId;
    private final String executionId;
    private final TaskStatus status;
    private final String message;

    public TaskResult(String taskId, String executionId, TaskStatus status, String message) {
        this.taskId = Objects.requireNonNull(taskId, "taskId");
        this.executionId = executionId;
        this.status = Objects.requireNonNull(status, "status");
        this.message = message;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
