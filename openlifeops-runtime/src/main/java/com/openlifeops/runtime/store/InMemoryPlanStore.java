package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Plan;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryPlanStore implements PlanStore {

    private final Map<String, Plan> byId = new ConcurrentHashMap<>();

    @Override
    public Plan save(Plan plan) {
        byId.put(plan.getId(), plan);
        return plan;
    }

    @Override
    public Optional<Plan> findById(String planId) {
        return Optional.ofNullable(byId.get(planId));
    }

    @Override
    public Optional<Plan> findByExecutionId(String executionId) {
        return byId.values().stream()
                .filter(plan -> plan.getExecutionId().equals(executionId))
                .findFirst();
    }
}
