package com.openlifeops.core.workflow;

import com.openlifeops.core.descriptor.WorkflowDefinition;

public record ResolvedWorkflow(
        WorkflowDefinition definition,
        String workflowVersion,
        java.util.List<WorkflowStepTemplate> steps) {
}
