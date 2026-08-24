package com.openlifeops.runtime;

import com.openlifeops.core.api.ApprovalRequest;
import com.openlifeops.core.api.TaskExecution;
import com.openlifeops.core.api.TaskRequest;
import com.openlifeops.core.api.TaskResult;
import com.openlifeops.core.domain.Approval;
import com.openlifeops.core.domain.ApprovalDecision;
import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.ExecutionStatus;
import com.openlifeops.core.domain.Plan;
import com.openlifeops.core.domain.Task;
import com.openlifeops.core.domain.TaskStatus;
import com.openlifeops.core.event.TaskCreated;
import com.openlifeops.core.event.PlanCreated;
import com.openlifeops.core.pack.InvalidTaskStateException;
import com.openlifeops.core.pack.OpenLifeOpsPack;
import com.openlifeops.core.pack.TaskNotFoundException;
import com.openlifeops.core.workflow.ResolvedWorkflow;
import com.openlifeops.runtime.pack.PackRegistry;
import com.openlifeops.runtime.planning.Planner;
import com.openlifeops.runtime.store.ApprovalStore;
import com.openlifeops.runtime.store.ExecutionStore;
import com.openlifeops.runtime.store.PlanStore;
import com.openlifeops.runtime.store.TaskStore;
import com.openlifeops.core.event.DomainEventPublisher;

import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

public final class TaskManager {

    private final TaskStore taskStore;
    private final ExecutionStore executionStore;
    private final PlanStore planStore;
    private final ApprovalStore approvalStore;
    private final PackRegistry packRegistry;
    private final Planner planner;
    private final ExecutionEngine executionEngine;
    private final DomainEventPublisher eventPublisher;

    public TaskManager(
            TaskStore taskStore,
            ExecutionStore executionStore,
            PlanStore planStore,
            ApprovalStore approvalStore,
            PackRegistry packRegistry,
            Planner planner,
            ExecutionEngine executionEngine,
            DomainEventPublisher eventPublisher) {
        this.taskStore = taskStore;
        this.executionStore = executionStore;
        this.planStore = planStore;
        this.approvalStore = approvalStore;
        this.packRegistry = packRegistry;
        this.planner = planner;
        this.executionEngine = executionEngine;
        this.eventPublisher = eventPublisher;
    }

    public TaskExecution execute(TaskRequest request) {
        OpenLifeOpsPack pack = packRegistry.require(request.getPack());
        ResolvedWorkflow workflow = packRegistry.getDefaultWorkflow(pack.id());
        Instant now = Instant.now();

        Task task = new Task(UUID.randomUUID().toString(), request.getObjective(), pack.id(), TaskStatus.CREATED, now);
        taskStore.save(task);
        eventPublisher.publish(new TaskCreated(task.getId(), pack.id(), now));

        Execution execution = createExecution(task, 1, now);
        Plan plan = planner.plan(task, execution, workflow);
        planStore.save(plan);
        eventPublisher.publish(new PlanCreated(task.getId(), execution.getId(), plan.getId(), now));

        bindExecutionToPlan(execution, plan);
        executionStore.save(execution);
        task.addExecutionId(execution.getId());
        taskStore.save(task);

        ExecutionRunOutcome outcome = executionEngine.run(task, execution, plan);
        return toExecution(task, execution, messageFor(outcome));
    }

    public synchronized TaskExecution approve(String taskId, ApprovalRequest approvalRequest) {
        Task task = taskStore.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));

        Execution execution = executionStore.findByTaskId(taskId).stream()
                .filter(candidate -> candidate.getStatus() == ExecutionStatus.AWAITING_APPROVAL)
                .findFirst()
                .orElse(null);

        if (execution == null) {
            Execution latest = getLatestExecution(taskId);
            return approvalStore.findByExecutionIdAndActionId(latest.getId(), approvalRequest.getActionId())
                    .filter(Approval::isDecided)
                    .map(approval -> toExecution(task, latest, "Approval already processed"))
                    .orElseThrow(() -> new InvalidTaskStateException(
                            "No pending execution for task " + taskId));
        }

        if (!approvalRequest.getActionId().equals(execution.getPendingActionId())) {
            throw new InvalidTaskStateException("Approval actionId does not match pending action");
        }

        Approval approval = approvalStore.findByExecutionIdAndActionId(
                        execution.getId(), approvalRequest.getActionId())
                .orElseThrow(() -> new InvalidTaskStateException("Pending approval not found"));

        if (approval.isDecided()) {
            return toExecution(task, execution, "Approval already processed");
        }

        approval.decide(
                approvalRequest.getDecision(),
                approvalRequest.getDecidedBy(),
                approvalRequest.getComment(),
                Instant.now());
        approvalStore.save(approval);

        if (approvalRequest.getDecision() == ApprovalDecision.REJECTED) {
            execution.setStatus(ExecutionStatus.CANCELLED);
            task.setStatus(TaskStatus.CANCELLED);
            executionStore.save(execution);
            taskStore.save(task);
            return toExecution(task, execution, "Approval rejected");
        }

        Plan plan = planStore.findByExecutionId(execution.getId())
                .orElseThrow(() -> new InvalidTaskStateException("Plan not found for execution"));

        if (execution.hasExecutedAction(approvalRequest.getActionId())) {
            execution.setStatus(ExecutionStatus.RUNNING);
            execution.clearPendingApproval();
            int nextIndex = findStepIndex(plan, approvalRequest.getActionId()) + 1;
            execution.setCurrentStepIndex(nextIndex);
            ExecutionRunOutcome outcome = executionEngine.run(task, execution, plan);
            return toExecution(task, execution, messageFor(outcome));
        }

        ExecutionRunOutcome outcome = executionEngine.resumeAfterApproval(
                task, execution, plan, approvalRequest.getActionId());
        return toExecution(task, execution, messageFor(outcome));
    }

    public TaskExecution retry(String taskId) {
        Task task = taskStore.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        Execution latest = getLatestExecution(taskId);
        if (latest.getStatus() != ExecutionStatus.FAILED && latest.getStatus() != ExecutionStatus.CANCELLED) {
            throw new InvalidTaskStateException("Latest execution is not retryable: " + latest.getStatus());
        }

        ResolvedWorkflow workflow = packRegistry.getDefaultWorkflow(task.getPackId());
        Instant now = Instant.now();
        Execution execution = createExecution(task, latest.getAttemptNumber() + 1, now);
        Plan plan = planner.plan(task, execution, workflow);
        planStore.save(plan);
        eventPublisher.publish(new PlanCreated(task.getId(), execution.getId(), plan.getId(), now));

        bindExecutionToPlan(execution, plan);
        executionStore.save(execution);
        task.addExecutionId(execution.getId());
        task.setStatus(TaskStatus.ACTIVE);
        taskStore.save(task);

        ExecutionRunOutcome outcome = executionEngine.run(task, execution, plan);
        return toExecution(task, execution, messageFor(outcome));
    }

    public Task getTask(String taskId) {
        return taskStore.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    public Execution getLatestExecution(String taskId) {
        return executionStore.findByTaskId(taskId).stream()
                .max(Comparator.comparingInt(Execution::getAttemptNumber))
                .orElseThrow(() -> new InvalidTaskStateException("No execution for task " + taskId));
    }

    public Plan getPlanForExecution(String executionId) {
        return planStore.findByExecutionId(executionId)
                .orElseThrow(() -> new InvalidTaskStateException("Plan not found for execution " + executionId));
    }

    private Execution createExecution(Task task, int attemptNumber, Instant now) {
        return new Execution(
                UUID.randomUUID().toString(),
                task.getId(),
                attemptNumber,
                ExecutionStatus.CREATED,
                now);
    }

    private void bindExecutionToPlan(Execution execution, Plan plan) {
        execution.setPlanId(plan.getId());
        execution.setWorkflowId(plan.getWorkflowId());
        execution.setSteps(plan.getSteps());
        execution.setCurrentStepIndex(0);
    }

    private int findStepIndex(Plan plan, String actionId) {
        for (int index = 0; index < plan.getSteps().size(); index++) {
            if (plan.getSteps().get(index).getAction().getId().equals(actionId)) {
                return index;
            }
        }
        throw new InvalidTaskStateException("Action not found in plan: " + actionId);
    }

    private String messageFor(ExecutionRunOutcome outcome) {
        return switch (outcome) {
            case COMPLETED -> "Task completed";
            case AWAITING_APPROVAL -> "Awaiting human approval";
            case FAILED -> "Execution failed";
        };
    }

    private TaskExecution toExecution(Task task, Execution execution, String message) {
        executionStore.save(execution);
        taskStore.save(task);
        TaskResult result = new TaskResult(task.getId(), execution.getId(), task.getStatus(), message);
        return new TaskExecution(task, execution, result);
    }
}
