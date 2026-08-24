package com.openlifeops.runtime.validation;

import com.openlifeops.core.domain.Execution;
import com.openlifeops.core.domain.Observation;
import com.openlifeops.core.domain.Step;
import com.openlifeops.core.domain.Task;
import com.openlifeops.core.domain.ValidationResult;

public interface Validator {

    ValidationResult validate(Task task, Execution execution, Step step, Observation observation);
}
