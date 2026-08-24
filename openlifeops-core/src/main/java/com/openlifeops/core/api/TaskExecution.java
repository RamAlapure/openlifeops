package com.openlifeops.core.api;

import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.Task;

import java.util.Objects;

public final class TaskExecution {

    private final Task task;
    private final Execution execution;
    private final TaskResult result;

    public TaskExecution(Task task, Execution execution, TaskResult result) {
        this.task = Objects.requireNonNull(task, "task");
        this.execution = Objects.requireNonNull(execution, "execution");
        this.result = Objects.requireNonNull(result, "result");
    }

    public Task getTask() {
        return task;
    }

    public Execution getExecution() {
        return execution;
    }

    public TaskResult getResult() {
        return result;
    }
}
