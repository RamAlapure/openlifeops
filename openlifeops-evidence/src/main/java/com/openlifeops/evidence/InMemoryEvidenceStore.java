package com.openlifeops.evidence;

import com.openlifeops.core.domain.Evidence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemoryEvidenceStore implements EvidenceStore {

    private final Map<String, Evidence> byId = new ConcurrentHashMap<>();

    @Override
    public Evidence append(Evidence evidence) {
        byId.put(evidence.getId(), evidence);
        return evidence;
    }

    @Override
    public Optional<Evidence> findById(String evidenceId) {
        return Optional.ofNullable(byId.get(evidenceId));
    }

    @Override
    public List<Evidence> findByTaskId(String taskId) {
        return byId.values().stream()
                .filter(evidence -> evidence.getTaskId().equals(taskId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Evidence> findByExecutionId(String executionId) {
        return byId.values().stream()
                .filter(evidence -> evidence.getExecutionId().equals(executionId))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
