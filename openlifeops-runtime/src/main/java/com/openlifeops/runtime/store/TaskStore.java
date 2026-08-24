package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Task;

import java.util.Optional;

public interface TaskStore {

    Task save(Task task);

    Optional<Task> findById(String taskId);
}
