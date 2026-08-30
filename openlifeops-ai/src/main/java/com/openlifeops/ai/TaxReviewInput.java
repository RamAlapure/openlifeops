package com.openlifeops.ai;

import java.util.List;
import java.util.Objects;

public record TaxReviewInput(
        String taskId,
        String reportId,
        String reportStatus,
        String reportJson,
        List<String> allowedCitationIds) {

    public TaxReviewInput {
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(reportId, "reportId");
        Objects.requireNonNull(reportStatus, "reportStatus");
        Objects.requireNonNull(reportJson, "reportJson");
        allowedCitationIds = List.copyOf(allowedCitationIds == null ? List.of() : allowedCitationIds);
    }
}
