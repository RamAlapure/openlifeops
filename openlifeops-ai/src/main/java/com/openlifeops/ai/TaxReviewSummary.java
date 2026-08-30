package com.openlifeops.ai;

import java.util.List;

public record TaxReviewSummary(
        String taskId,
        String reportId,
        String reportStatus,
        String summary,
        List<String> concerns,
        List<String> questions,
        List<String> citationIds,
        String provider) {

    public TaxReviewSummary {
        concerns = List.copyOf(concerns == null ? List.of() : concerns);
        questions = List.copyOf(questions == null ? List.of() : questions);
        citationIds = List.copyOf(citationIds == null ? List.of() : citationIds);
    }
}
