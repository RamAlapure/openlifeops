package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemoryExecutionStore implements ExecutionStore {

    private final Map<String, Execution> executions = new ConcurrentHashMap<>();

    @Override
    public Execution save(Execution execution) {
        executions.put(execution.getId(), execution);
        return execution;
    }

    @Override
    public Optional<Execution> findById(String executionId) {
        return Optional.ofNullable(executions.get(executionId));
    }

    @Override
    public List<Execution> findByTaskId(String taskId) {
        return executions.values().stream()
                .filter(execution -> execution.getTaskId().equals(taskId))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
