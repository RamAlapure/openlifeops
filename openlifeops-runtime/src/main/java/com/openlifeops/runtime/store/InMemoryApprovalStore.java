package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Approval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemoryApprovalStore implements ApprovalStore {

    private final Map<String, Approval> byId = new ConcurrentHashMap<>();

    @Override
    public Approval save(Approval approval) {
        byId.put(approval.getId(), approval);
        return approval;
    }

    @Override
    public List<Approval> findByTaskId(String taskId) {
        return byId.values().stream()
                .filter(approval -> approval.getTaskId().equals(taskId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Optional<Approval> findByExecutionIdAndActionId(String executionId, String actionId) {
        return byId.values().stream()
                .filter(approval -> approval.getExecutionId().equals(executionId)
                        && approval.getActionId().equals(actionId))
                .findFirst();
    }
}
