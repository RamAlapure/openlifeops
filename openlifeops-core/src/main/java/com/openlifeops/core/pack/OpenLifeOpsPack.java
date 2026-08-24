package com.openlifeops.core.pack;

import com.openlifeops.core.descriptor.PolicyDefinition;
import com.openlifeops.core.descriptor.ToolDescriptor;
import com.openlifeops.core.descriptor.WorkflowDefinition;
import com.openlifeops.core.mcp.McpToolReference;
import com.openlifeops.core.workflow.WorkflowStepTemplate;

import java.util.List;
import java.util.Map;

public interface OpenLifeOpsPack {

    String id();

    String name();

    List<WorkflowDefinition> workflows();

    List<ToolDescriptor> tools();

    List<PolicyDefinition> policies();

    default String workflowVersion(String workflowId) {
        return "1";
    }

    default List<WorkflowStepTemplate> workflowSteps(String workflowId) {
        return List.of();
    }

    default Map<String, McpToolReference> mcpToolBindings() {
        return Map.of();
    }
}
