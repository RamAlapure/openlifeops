package com.openlifeops.runtime;

import com.openlifeops.core.domain.Action;
import com.openlifeops.core.domain.Approval;
import com.openlifeops.core.domain.ApprovalStatus;
import com.openlifeops.core.domain.CheckpointType;
import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.ExecutionCheckpoint;
import com.openlifeops.core.domain.ExecutionStatus;
import com.openlifeops.core.domain.Observation;
import com.openlifeops.core.domain.Plan;
import com.openlifeops.core.domain.PolicyDecision;
import com.openlifeops.core.domain.Step;
import com.openlifeops.core.domain.StepStatus;
import com.openlifeops.core.domain.Task;
import com.openlifeops.core.domain.TaskStatus;
import com.openlifeops.core.domain.ValidationResult;
import com.openlifeops.core.event.ApprovalRequired;
import com.openlifeops.core.event.DomainEventPublisher;
import com.openlifeops.core.event.ExecutionCheckpointed;
import com.openlifeops.core.event.ExecutionFailed;
import com.openlifeops.core.event.ExecutionStarted;
import com.openlifeops.core.event.StepCompleted;
import com.openlifeops.core.event.StepStarted;
import com.openlifeops.core.event.TaskCompleted;
import com.openlifeops.core.event.ValidationFailed;
import com.openlifeops.governance.PolicyEngine;
import com.openlifeops.runtime.store.ApprovalStore;
import com.openlifeops.runtime.store.ExecutionStore;
import com.openlifeops.runtime.store.TaskStore;
import com.openlifeops.runtime.validation.Validator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ExecutionEngine {

    private final PolicyEngine policyEngine;
    private final Executor executor;
    private final Validator validator;
    private final TaskStore taskStore;
    private final ExecutionStore executionStore;
    private final ApprovalStore approvalStore;
    private final DomainEventPublisher eventPublisher;

    public ExecutionEngine(
            PolicyEngine policyEngine,
            Executor executor,
            Validator validator,
            TaskStore taskStore,
            ExecutionStore executionStore,
            ApprovalStore approvalStore,
            DomainEventPublisher eventPublisher) {
        this.policyEngine = policyEngine;
        this.executor = executor;
        this.validator = validator;
        this.taskStore = taskStore;
        this.executionStore = executionStore;
        this.approvalStore = approvalStore;
        this.eventPublisher = eventPublisher;
    }

    public ExecutionRunOutcome run(Task task, Execution execution, Plan plan) {
        if (execution.getStatus() == ExecutionStatus.CREATED) {
            execution.setStatus(ExecutionStatus.RUNNING);
            eventPublisher.publish(new ExecutionStarted(task.getId(), execution.getId(), Instant.now()));
        } else if (execution.getStatus() == ExecutionStatus.AWAITING_APPROVAL) {
            execution.setStatus(ExecutionStatus.RUNNING);
        }
        task.setStatus(TaskStatus.ACTIVE);
        return runFromIndex(task, execution, plan, execution.getCurrentStepIndex());
    }

    public ExecutionRunOutcome resumeAfterApproval(Task task, Execution execution, Plan plan, String actionId) {
        if (execution.getStatus() != ExecutionStatus.AWAITING_APPROVAL) {
            return ExecutionRunOutcome.FAILED;
        }
        if (!actionId.equals(execution.getPendingActionId())) {
            throw new IllegalArgumentException("Approval actionId does not match pending action");
        }
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.clearPendingApproval();
        task.setStatus(TaskStatus.ACTIVE);

        int stepIndex = findStepIndex(plan, actionId);
        Step step = plan.getSteps().get(stepIndex);
        Action action = step.getAction();

        if (!execution.hasExecutedAction(actionId)) {
            ExecutionRunOutcome stepOutcome = executeApprovedStep(task, execution, plan, stepIndex, step, action);
            if (stepOutcome != ExecutionRunOutcome.COMPLETED && stepOutcome != ExecutionRunOutcome.AWAITING_APPROVAL) {
                return stepOutcome;
            }
        }
        int nextIndex = stepIndex + 1;
        execution.setCurrentStepIndex(nextIndex);
        return runFromIndex(task, execution, plan, nextIndex);
    }

    private ExecutionRunOutcome runFromIndex(Task task, Execution execution, Plan plan, int startIndex) {
        List<Step> steps = plan.getSteps();
        for (int index = startIndex; index < steps.size(); index++) {
            Step step = steps.get(index);
            Action action = step.getAction();
            execution.setCurrentStepIndex(index);
            execution.updateStepStatus(step.getId(), StepStatus.RUNNING);
            eventPublisher.publish(new StepStarted(task.getId(), execution.getId(), step.getId(), Instant.now()));

            PolicyDecision decision = policyEngine.evaluate(action);
            if (decision == PolicyDecision.DENY) {
                return failExecution(task, execution, plan, index, step, action, "Action denied by policy");
            }
            if (decision == PolicyDecision.REQUIRE_APPROVAL && !execution.hasExecutedAction(action.getId())) {
                return pauseForApproval(task, execution, plan, index, step, action);
            }

            ExecutionRunOutcome stepOutcome = executeAndValidateStep(task, execution, plan, index, step, action);
            if (stepOutcome != ExecutionRunOutcome.COMPLETED) {
                return stepOutcome;
            }
        }
        return completeExecution(task, execution);
    }

    private ExecutionRunOutcome executeApprovedStep(
            Task task, Execution execution, Plan plan, int index, Step step, Action action) {
        return executeAndValidateStep(task, execution, plan, index, step, action);
    }

    private ExecutionRunOutcome executeAndValidateStep(
            Task task, Execution execution, Plan plan, int index, Step step, Action action) {
        Observation observation;
        try {
            observation = executor.execute(task, execution, step, action).orElse(null);
        } catch (RuntimeException exception) {
            execution.updateStepStatus(step.getId(), StepStatus.FAILED);
            return failExecution(task, execution, plan, index, step, action, exception.getMessage());
        }
        ValidationResult validation = validator.validate(task, execution, step, observation);
        if (!validation.isPassed()) {
            eventPublisher.publish(new ValidationFailed(
                    task.getId(), execution.getId(), step.getId(), validation.getMessage(), Instant.now()));
            execution.updateStepStatus(step.getId(), StepStatus.FAILED);
            return failExecution(task, execution, plan, index, step, action, validation.getMessage());
        }
        execution.updateStepStatus(step.getId(), StepStatus.COMPLETED);
        eventPublisher.publish(new StepCompleted(task.getId(), execution.getId(), step.getId(), Instant.now()));
        saveCheckpoint(task, execution, plan, index, step, action, CheckpointType.STEP_COMPLETED);
        execution.setCurrentStepIndex(index + 1);
        persist(task, execution);
        return ExecutionRunOutcome.COMPLETED;
    }

    private ExecutionRunOutcome pauseForApproval(
            Task task, Execution execution, Plan plan, int index, Step step, Action action) {
        Approval approval = approvalStore.findByExecutionIdAndActionId(execution.getId(), action.getId())
                .orElseGet(() -> {
                    Approval pending = new Approval(
                            UUID.randomUUID().toString(),
                            task.getId(),
                            execution.getId(),
                            action.getId(),
                            ApprovalStatus.PENDING,
                            Instant.now());
                    return approvalStore.save(pending);
                });

        if (approval.isDecided() && approval.getDecision() == com.openlifeops.core.domain.ApprovalDecision.APPROVED) {
            return executeAndValidateStep(task, execution, plan, index, step, action);
        }

        execution.setStatus(ExecutionStatus.AWAITING_APPROVAL);
        execution.setPendingActionId(action.getId());
        execution.setPendingStepId(step.getId());
        saveCheckpoint(task, execution, plan, index, step, action, CheckpointType.APPROVAL_REQUIRED);
        eventPublisher.publish(new ApprovalRequired(task.getId(), execution.getId(), action.getId(), Instant.now()));
        task.setStatus(TaskStatus.ACTIVE);
        persist(task, execution);
        return ExecutionRunOutcome.AWAITING_APPROVAL;
    }

    private ExecutionRunOutcome failExecution(
            Task task,
            Execution execution,
            Plan plan,
            int index,
            Step step,
            Action action,
            String message) {
        execution.setStatus(ExecutionStatus.FAILED);
        task.setStatus(TaskStatus.ACTIVE);
        saveCheckpoint(task, execution, plan, index, step, action, CheckpointType.EXECUTION_FAILED);
        eventPublisher.publish(new ExecutionFailed(task.getId(), execution.getId(), message, Instant.now()));
        persist(task, execution);
        return ExecutionRunOutcome.FAILED;
    }

    private ExecutionRunOutcome completeExecution(Task task, Execution execution) {
        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.clearPendingApproval();
        task.setStatus(TaskStatus.COMPLETED);
        persist(task, execution);
        eventPublisher.publish(new TaskCompleted(task.getId(), execution.getId(), Instant.now()));
        return ExecutionRunOutcome.COMPLETED;
    }

    private void saveCheckpoint(
            Task task,
            Execution execution,
            Plan plan,
            int index,
            Step step,
            Action action,
            CheckpointType checkpointType) {
        ExecutionCheckpoint checkpoint = new ExecutionCheckpoint(
                execution.getId(),
                plan.getId(),
                index,
                step.getId(),
                action.getId(),
                checkpointType,
                Instant.now());
        execution.recordCheckpoint(checkpoint);
        eventPublisher.publish(new ExecutionCheckpointed(
                task.getId(),
                execution.getId(),
                action.getId(),
                checkpointType.name(),
                checkpoint.getCreatedAt()));
    }

    private int findStepIndex(Plan plan, String actionId) {
        List<Step> steps = plan.getSteps();
        for (int index = 0; index < steps.size(); index++) {
            if (steps.get(index).getAction().getId().equals(actionId)) {
                return index;
            }
        }
        throw new IllegalArgumentException("Action not found in plan: " + actionId);
    }

    private void persist(Task task, Execution execution) {
        executionStore.save(execution);
        taskStore.save(task);
    }
}
