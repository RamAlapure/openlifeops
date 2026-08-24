package com.openlifeops.evidence;

import com.openlifeops.core.domain.Evidence;

import java.util.List;
import java.util.Optional;

public interface EvidenceStore {

    Evidence append(Evidence evidence);

    Optional<Evidence> findById(String evidenceId);

    List<Evidence> findByTaskId(String taskId);

    List<Evidence> findByExecutionId(String executionId);
}
