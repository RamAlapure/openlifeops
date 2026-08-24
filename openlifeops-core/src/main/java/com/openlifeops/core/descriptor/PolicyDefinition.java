package com.openlifeops.core.descriptor;

import com.openlifeops.core.domain.ActionType;
import com.openlifeops.core.domain.RiskLevel;

public record PolicyDefinition(
        String id,
        ActionType actionType,
        RiskLevel risk,
        boolean approvalRequired) {
}
