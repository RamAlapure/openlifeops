package com.openlifeops.core.workflow;

import com.openlifeops.core.domain.ActionType;
import com.openlifeops.core.domain.RiskLevel;

public record WorkflowStepTemplate(
        String stepKey,
        String name,
        int order,
        ActionType actionType,
        String target,
        RiskLevel riskLevel,
        String toolId) {
}
