package com.openlifeops.runtime.planning;

import com.openlifeops.core.domain.Action;
import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.Plan;
import com.openlifeops.core.domain.Step;
import com.openlifeops.core.domain.StepStatus;
import com.openlifeops.core.domain.Task;
import com.openlifeops.core.workflow.ResolvedWorkflow;
import com.openlifeops.core.workflow.WorkflowStepTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RuleBasedPlanner implements Planner {

    @Override
    public Plan plan(Task task, Execution execution, ResolvedWorkflow workflow) {
        List<Step> steps = new ArrayList<>();
        for (WorkflowStepTemplate template : workflow.steps()) {
            String stepId = UUID.randomUUID().toString();
            String actionId = UUID.randomUUID().toString();
            Action action = new Action(
                    actionId,
                    template.actionType(),
                    template.target(),
                    template.toolId(),
                    template.riskLevel(),
                    template.name(),
                    Map.of("stepKey", template.stepKey()));
            steps.add(new Step(
                    stepId,
                    template.stepKey(),
                    template.name(),
                    template.order(),
                    action,
                    StepStatus.PENDING));
        }
        return new Plan(
                UUID.randomUUID().toString(),
                task.getId(),
                execution.getId(),
                workflow.definition().id(),
                workflow.workflowVersion(),
                Instant.now(),
                steps);
    }
}
