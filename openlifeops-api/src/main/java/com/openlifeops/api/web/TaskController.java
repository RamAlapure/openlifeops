package com.openlifeops.api.web;

import com.openlifeops.core.api.ApprovalRequest;
import com.openlifeops.core.api.TaskExecution;
import com.openlifeops.core.api.TaskRequest;
import com.openlifeops.core.domain.ApprovalDecision;
import com.openlifeops.core.domain.ExecutionStatus;
import com.openlifeops.core.domain.Observation;
import com.openlifeops.core.domain.Step;
import com.openlifeops.core.domain.StepStatus;
import com.openlifeops.core.domain.TaskStatus;
import com.openlifeops.core.pack.InvalidTaskStateException;
import com.openlifeops.core.pack.PackNotFoundException;
import com.openlifeops.core.pack.TaskNotFoundException;
import com.openlifeops.evidence.EvidenceStore;
import com.openlifeops.orchestrator.OpenLifeOps;
import com.openlifeops.runtime.TaskManager;
import com.openlifeops.runtime.TaskView;
import com.openlifeops.runtime.store.ObservationStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final OpenLifeOps openLifeOps;
    private final TaskManager taskManager;
    private final EvidenceStore evidenceStore;
    private final ObservationStore observationStore;

    public TaskController(
            OpenLifeOps openLifeOps,
            TaskManager taskManager,
            EvidenceStore evidenceStore,
            ObservationStore observationStore) {
        this.openLifeOps = openLifeOps;
        this.taskManager = taskManager;
        this.evidenceStore = evidenceStore;
        this.observationStore = observationStore;
    }

    @PostMapping
    public TaskExecutionResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskExecution execution = openLifeOps.execute(new TaskRequest(request.objective(), request.pack()));
        return TaskExecutionResponse.from(execution);
    }

    @GetMapping("/{taskId}")
    public TaskDetailResponse getTask(@PathVariable String taskId) {
        TaskView view = TaskView.load(taskManager, evidenceStore, observationStore, taskId);
        return TaskDetailResponse.from(view, evidenceStore, observationStore);
    }

    @PostMapping("/{taskId}/approvals")
    public TaskExecutionResponse approve(
            @PathVariable String taskId,
            @Valid @RequestBody SubmitApprovalRequest request) {
        TaskExecution execution = openLifeOps.approve(
                taskId,
                new ApprovalRequest(request.actionId(), request.decision(), request.decidedBy(), request.comment()));
        return TaskExecutionResponse.from(execution);
    }

    @PostMapping("/{taskId}/retry")
    public TaskExecutionResponse retry(@PathVariable String taskId) {
        TaskExecution execution = openLifeOps.retry(taskId);
        return TaskExecutionResponse.from(execution);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    ResponseEntity<Map<String, String>> handleNotFound(TaskNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler({PackNotFoundException.class, InvalidTaskStateException.class})
    ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }

    public record CreateTaskRequest(
            @NotBlank String objective,
            @NotBlank String pack) {
    }

    public record SubmitApprovalRequest(
            @NotBlank String actionId,
            @NotNull ApprovalDecision decision,
            String decidedBy,
            String comment) {
    }

    public record TaskExecutionResponse(
            String taskId,
            String executionId,
            TaskStatus taskStatus,
            ExecutionStatus executionStatus,
            String message) {

        static TaskExecutionResponse from(TaskExecution execution) {
            return new TaskExecutionResponse(
                    execution.getTask().getId(),
                    execution.getExecution().getId(),
                    execution.getTask().getStatus(),
                    execution.getExecution().getStatus(),
                    execution.getResult().getMessage());
        }
    }

    public record StepResponse(
            String id,
            String stepKey,
            String name,
            int order,
            String actionId,
            String actionType,
            String actionTarget,
            String toolId,
            StepStatus status) {

        static StepResponse from(Step step) {
            return new StepResponse(
                    step.getId(),
                    step.getStepKey(),
                    step.getName(),
                    step.getOrder(),
                    step.getAction().getId(),
                    step.getAction().getType().name(),
                    step.getAction().getTarget(),
                    step.getAction().getToolId(),
                    step.getStatus());
        }
    }

    public record EvidenceResponse(
            String id,
            String source,
            String sourceReference,
            String claim,
            String extractedValue,
            String provenance,
            Map<String, String> metadata) {

        static EvidenceResponse from(com.openlifeops.core.domain.Evidence evidence) {
            return new EvidenceResponse(
                    evidence.getId(),
                    evidence.getSource(),
                    evidence.getSourceReference(),
                    evidence.getClaim(),
                    evidence.getExtractedValue(),
                    evidence.getProvenance(),
                    evidence.getMetadata());
        }
    }

    public record ObservationResponse(
            String id,
            String actionId,
            String output) {

        static ObservationResponse from(Observation observation) {
            return new ObservationResponse(
                    observation.getId(),
                    observation.getActionId(),
                    observation.getOutput());
        }
    }

    public record TaskDetailResponse(
            String taskId,
            String objective,
            String packId,
            TaskStatus taskStatus,
            String executionId,
            int attemptNumber,
            ExecutionStatus executionStatus,
            String planId,
            String workflowId,
            String workflowVersion,
            int currentStepIndex,
            String pendingActionId,
            List<StepResponse> steps,
            List<EvidenceResponse> evidence,
            List<ObservationResponse> observations) {

        static TaskDetailResponse from(TaskView view, EvidenceStore evidenceStore, ObservationStore observationStore) {
            return new TaskDetailResponse(
                    view.getTask().getId(),
                    view.getTask().getObjective(),
                    view.getTask().getPackId(),
                    view.getTask().getStatus(),
                    view.getExecution().getId(),
                    view.getExecution().getAttemptNumber(),
                    view.getExecution().getStatus(),
                    view.getPlan().getId(),
                    view.getPlan().getWorkflowId(),
                    view.getPlan().getWorkflowVersion(),
                    view.getExecution().getCurrentStepIndex(),
                    view.getExecution().getPendingActionId(),
                    view.getExecution().getSteps().stream().map(StepResponse::from).toList(),
                    evidenceStore.findByTaskId(view.getTask().getId()).stream().map(EvidenceResponse::from).toList(),
                    observationStore.findByTaskId(view.getTask().getId()).stream().map(ObservationResponse::from).toList());
        }
    }
}
