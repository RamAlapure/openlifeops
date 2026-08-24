package com.openlifeops.runtime.planning;

import com.openlifeops.core.domain.Action;
import com.openlifeops.core.domain.Plan;
import com.openlifeops.core.domain.Step;
import com.openlifeops.core.domain.StepStatus;
import com.openlifeops.core.domain.Task;
import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.workflow.ResolvedWorkflow;
import com.openlifeops.core.workflow.WorkflowStepTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Planner {

    Plan plan(Task task, Execution execution, ResolvedWorkflow workflow);
}
