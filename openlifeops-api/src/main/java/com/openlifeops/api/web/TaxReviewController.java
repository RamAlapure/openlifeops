package com.openlifeops.api.web;

import com.openlifeops.ai.AiReviewService;
import com.openlifeops.ai.TaxReviewInput;
import com.openlifeops.ai.TaxReviewSummary;
import com.openlifeops.core.domain.Evidence;
import com.openlifeops.evidence.EvidenceStore;
import com.openlifeops.runtime.TaskManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/tasks")
public final class TaxReviewController {

    private static final Pattern CITATION_ID = Pattern.compile("\\\"(?:documentId|chunkId)\\\":\\\"([^\\\"]+)\\\"");

    private final TaskManager taskManager;
    private final EvidenceStore evidenceStore;
    private final AiReviewService aiReviewService;

    public TaxReviewController(TaskManager taskManager, EvidenceStore evidenceStore, AiReviewService aiReviewService) {
        this.taskManager = taskManager;
        this.evidenceStore = evidenceStore;
        this.aiReviewService = aiReviewService;
    }

    @PostMapping("/{taskId}/review-summary")
    public TaxReviewSummary review(@PathVariable String taskId) {
        taskManager.getTask(taskId);
        var execution = taskManager.getLatestExecution(taskId);
        Evidence report = evidenceStore.findByExecutionId(execution.getId()).stream()
                .filter(evidence -> "TaxReconciliationReport".equals(evidence.getMetadata().get("artifactType")))
                .findFirst()
                .orElseThrow(() -> new ReviewSummaryNotAvailableException(taskId));

        String reportId = report.getMetadata().get("reportId");
        String reportStatus = report.getMetadata().getOrDefault("reportStatus", "UNKNOWN");
        if (reportId == null || report.getExtractedValue() == null) {
            throw new ReviewSummaryNotAvailableException(taskId);
        }
        return aiReviewService.review(new TaxReviewInput(
                taskId, reportId, reportStatus, report.getExtractedValue(), citationIds(report)));
    }

    private static List<String> citationIds(Evidence report) {
        String citations = report.getMetadata().getOrDefault("citations", "");
        Matcher matcher = CITATION_ID.matcher(citations);
        List<String> ids = new ArrayList<>();
        while (matcher.find()) {
            if (!ids.contains(matcher.group(1))) {
                ids.add(matcher.group(1));
            }
        }
        return List.copyOf(ids);
    }

    @ExceptionHandler(ReviewSummaryNotAvailableException.class)
    ResponseEntity<java.util.Map<String, String>> handleUnavailable(ReviewSummaryNotAvailableException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", exception.getMessage()));
    }

    static final class ReviewSummaryNotAvailableException extends RuntimeException {
        ReviewSummaryNotAvailableException(String taskId) {
            super("Tax reconciliation report is not available for task " + taskId);
        }
    }
}
