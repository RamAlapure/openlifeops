package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Observation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemoryObservationStore implements ObservationStore {

    private final Map<String, Observation> byId = new ConcurrentHashMap<>();

    @Override
    public Observation save(Observation observation) {
        byId.put(observation.getId(), observation);
        return observation;
    }

    @Override
    public Optional<Observation> findByExecutionIdAndActionId(String executionId, String actionId) {
        return byId.values().stream()
                .filter(observation -> observation.getExecutionId().equals(executionId)
                        && observation.getActionId().equals(actionId))
                .findFirst();
    }

    @Override
    public List<Observation> findByExecutionId(String executionId) {
        return byId.values().stream()
                .filter(observation -> observation.getExecutionId().equals(executionId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Observation> findByTaskId(String taskId) {
        return byId.values().stream()
                .filter(observation -> observation.getTaskId().equals(taskId))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
