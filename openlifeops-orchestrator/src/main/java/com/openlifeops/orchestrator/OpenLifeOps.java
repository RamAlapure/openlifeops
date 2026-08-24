package com.openlifeops.orchestrator;

import com.openlifeops.core.api.ApprovalRequest;
import com.openlifeops.core.api.TaskExecution;
import com.openlifeops.core.api.TaskRequest;
import com.openlifeops.runtime.TaskManager;

public final class OpenLifeOps {

    private final TaskManager taskManager;

    public OpenLifeOps(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public TaskExecution execute(TaskRequest request) {
        return taskManager.execute(request);
    }

    public TaskExecution approve(String taskId, ApprovalRequest approvalRequest) {
        return taskManager.approve(taskId, approvalRequest);
    }

    public TaskExecution retry(String taskId) {
        return taskManager.retry(taskId);
    }
}
