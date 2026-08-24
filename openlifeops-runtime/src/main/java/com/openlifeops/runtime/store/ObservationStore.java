package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Observation;

import java.util.List;
import java.util.Optional;

public interface ObservationStore {

    Observation save(Observation observation);

    Optional<Observation> findByExecutionIdAndActionId(String executionId, String actionId);

    List<Observation> findByExecutionId(String executionId);

    List<Observation> findByTaskId(String taskId);
}
