package com.openlifeops.runtime.validation;

import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.Observation;
import com.openlifeops.core.domain.Step;
import com.openlifeops.core.domain.Task;
import com.openlifeops.core.domain.ValidationResult;

public final class DefaultValidator implements Validator {

    @Override
    public ValidationResult validate(Task task, Execution execution, Step step, Observation observation) {
        if (observation == null || observation.getOutput().isBlank()) {
            return ValidationResult.fail("Missing observation output");
        }
        if (task.getObjective().contains("[fail-reconcile]")
                && "reconcile_ledger".equals(step.getStepKey())
                && execution.getAttemptNumber() == 1) {
            return ValidationResult.fail("Forced reconciliation failure for test");
        }
        return ValidationResult.pass("Step validated");
    }
}
