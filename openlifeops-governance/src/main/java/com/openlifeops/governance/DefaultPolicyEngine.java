package com.openlifeops.governance;

import com.openlifeops.core.descriptor.PolicyDefinition;
import com.openlifeops.core.domain.Action;
import com.openlifeops.core.domain.ActionType;
import com.openlifeops.core.domain.PolicyDecision;
import com.openlifeops.core.domain.RiskLevel;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class DefaultPolicyEngine implements PolicyEngine {

    private final Map<ActionType, PolicyDefinition> policiesByAction = new EnumMap<>(ActionType.class);

    public DefaultPolicyEngine(List<PolicyDefinition> policies) {
        for (PolicyDefinition policy : policies) {
            policiesByAction.put(policy.actionType(), policy);
        }
    }

    @Override
    public PolicyDecision evaluate(Action action) {
        PolicyDefinition policy = policiesByAction.get(action.getType());
        if (policy == null) {
            return fallbackForRisk(action.getRisk());
        }
        if (policy.approvalRequired() || policy.risk() == RiskLevel.HIGH || policy.risk() == RiskLevel.CRITICAL) {
            return PolicyDecision.REQUIRE_APPROVAL;
        }
        return PolicyDecision.ALLOW;
    }

    private PolicyDecision fallbackForRisk(RiskLevel risk) {
        return switch (risk) {
            case LOW, MEDIUM -> PolicyDecision.ALLOW;
            case HIGH, CRITICAL -> PolicyDecision.REQUIRE_APPROVAL;
        };
    }
}
