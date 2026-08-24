package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Execution;

import java.util.List;
import java.util.Optional;

public interface ExecutionStore {

    Execution save(Execution execution);

    Optional<Execution> findById(String executionId);

    List<Execution> findByTaskId(String taskId);
}
