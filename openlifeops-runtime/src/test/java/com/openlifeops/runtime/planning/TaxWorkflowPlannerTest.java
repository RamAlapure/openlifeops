package com.openlifeops.runtime.planning;

import com.openlifeops.core.descriptor.WorkflowDefinition;
import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.ExecutionStatus;
import com.openlifeops.core.domain.Plan;
import com.openlifeops.core.domain.Task;
import com.openlifeops.core.domain.TaskStatus;
import com.openlifeops.core.workflow.ResolvedWorkflow;
import com.openlifeops.packs.tax.TaxPack;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxWorkflowPlannerTest {

    @Test
    void planHasThreeOrderedSteps() {
        TaxPack pack = new TaxPack();
        ResolvedWorkflow workflow = new ResolvedWorkflow(
                pack.workflows().getFirst(),
                pack.workflowVersion(TaxPack.WORKFLOW_RECONCILE),
                pack.workflowSteps(TaxPack.WORKFLOW_RECONCILE));

        Task task = new Task("t1", "Reconcile", TaxPack.PACK_ID, TaskStatus.CREATED, Instant.now());
        Execution execution = new Execution("e1", "t1", 1, ExecutionStatus.CREATED, Instant.now());

        Planner planner = new RuleBasedPlanner();
        Plan plan = planner.plan(task, execution, workflow);

        assertEquals(3, plan.stepCount());
        assertEquals("2", plan.getWorkflowVersion());
        assertEquals("read_documents", plan.getSteps().get(0).getStepKey());
        assertEquals("reconcile_ledger", plan.getSteps().get(1).getStepKey());
        assertEquals("submit_report", plan.getSteps().get(2).getStepKey());
        assertEquals("tax.read_document", plan.getSteps().get(0).getAction().getToolId());
    }
}
