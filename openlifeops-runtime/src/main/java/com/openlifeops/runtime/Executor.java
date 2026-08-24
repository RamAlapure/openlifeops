package com.openlifeops.runtime;

import com.openlifeops.core.domain.Action;
import com.openlifeops.core.domain.Evidence;
import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.Observation;
import com.openlifeops.core.domain.Step;
import com.openlifeops.core.domain.Task;
import com.openlifeops.evidence.EvidenceStore;
import com.openlifeops.mcp.ToolInvocationResult;
import com.openlifeops.mcp.ToolRegistry;
import com.openlifeops.runtime.store.ObservationStore;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class Executor {

    private final ToolRegistry toolRegistry;
    private final ObservationStore observationStore;
    private final EvidenceStore evidenceStore;

    public Executor(ToolRegistry toolRegistry, ObservationStore observationStore, EvidenceStore evidenceStore) {
        this.toolRegistry = toolRegistry;
        this.observationStore = observationStore;
        this.evidenceStore = evidenceStore;
    }

    public Optional<Observation> execute(Task task, Execution execution, Step step, Action action) {
        synchronized (execution) {
            if (execution.hasExecutedAction(action.getId())) {
                return observationStore.findByExecutionIdAndActionId(execution.getId(), action.getId());
            }
            ToolInvocationResult invocation = toolRegistry.invoke(task.getPackId(), task.getObjective(), action);
            String output = invocation.output();
            Observation observation = new Observation(
                    UUID.randomUUID().toString(),
                    task.getId(),
                    execution.getId(),
                    action.getId(),
                    output,
                    Instant.now());
            observationStore.save(observation);
            Map<String, String> evidenceMetadata = new java.util.HashMap<>();
            evidenceMetadata.put("actionId", action.getId());
            evidenceMetadata.put("stepKey", step.getStepKey());
            evidenceMetadata.putAll(invocation.metadata());
            Evidence evidence = new Evidence(
                    UUID.randomUUID().toString(),
                    task.getId(),
                    execution.getId(),
                    invocation.metadata().containsKey("citation")
                            ? "knowledge:" + invocation.metadata().getOrDefault("documentId", "unknown")
                            : "tool:" + action.getToolId(),
                    action.getTarget(),
                    action.getDescription(),
                    output,
                    Instant.now(),
                    invocation.provenance(),
                    evidenceMetadata);
            evidenceStore.append(evidence);
            execution.markActionExecuted(action.getId());
            return Optional.of(observation);
        }
    }
}
