package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Plan;

import java.util.Optional;

public interface PlanStore {

    Plan save(Plan plan);

    Optional<Plan> findById(String planId);

    Optional<Plan> findByExecutionId(String executionId);
}
