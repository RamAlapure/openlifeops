package com.openlifeops.runtime.store;

import com.openlifeops.core.domain.Approval;

import java.util.List;
import java.util.Optional;

public interface ApprovalStore {

    Approval save(Approval approval);

    List<Approval> findByTaskId(String taskId);

    Optional<Approval> findByExecutionIdAndActionId(String executionId, String actionId);
}
