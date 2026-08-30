package com.openlifeops.ai;

import java.util.List;

/** Safe fallback used when no ChatModel is configured. */
public final class DeterministicTaxReviewService implements AiReviewService {

    @Override
    public TaxReviewSummary review(TaxReviewInput input) {
        boolean reviewRequired = "REVIEW_REQUIRED".equals(input.reportStatus());
        return new TaxReviewSummary(
                input.taskId(),
                input.reportId(),
                input.reportStatus(),
                reviewRequired
                        ? "The deterministic reconciliation found differences that require human review."
                        : "The deterministic reconciliation found no conflicting values in the available documents.",
                reviewRequired
                        ? List.of("Review the mismatched fields against the cited source documents.")
                        : List.of(),
                List.of("Confirm that the cited documents cover the intended tax year before approval."),
                input.allowedCitationIds(),
                "deterministic");
    }
}
