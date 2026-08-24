package com.openlifeops.runtime;

import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.Plan;
import com.openlifeops.core.domain.Task;
import com.openlifeops.evidence.EvidenceStore;
import com.openlifeops.runtime.store.ObservationStore;

import java.util.List;

public final class TaskView {

    private final Task task;
    private final Execution execution;
    private final Plan plan;
    private final List<String> evidenceIds;
    private final List<String> observationIds;

    public TaskView(
            Task task,
            Execution execution,
            Plan plan,
            List<String> evidenceIds,
            List<String> observationIds) {
        this.task = task;
        this.execution = execution;
        this.plan = plan;
        this.evidenceIds = evidenceIds;
        this.observationIds = observationIds;
    }

    public Task getTask() {
        return task;
    }

    public Execution getExecution() {
        return execution;
    }

    public Plan getPlan() {
        return plan;
    }

    public List<String> getEvidenceIds() {
        return evidenceIds;
    }

    public List<String> getObservationIds() {
        return observationIds;
    }

    public static TaskView load(
            TaskManager taskManager,
            EvidenceStore evidenceStore,
            ObservationStore observationStore,
            String taskId) {
        Task task = taskManager.getTask(taskId);
        Execution execution = taskManager.getLatestExecution(taskId);
        Plan plan = taskManager.getPlanForExecution(execution.getId());
        List<String> evidenceIds = evidenceStore.findByTaskId(taskId).stream()
                .map(evidence -> evidence.getId())
                .toList();
        List<String> observationIds = observationStore.findByTaskId(taskId).stream()
                .map(observation -> observation.getId())
                .toList();
        return new TaskView(task, execution, plan, evidenceIds, observationIds);
    }
}
