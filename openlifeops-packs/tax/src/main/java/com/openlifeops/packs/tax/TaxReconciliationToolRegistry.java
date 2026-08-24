package com.openlifeops.packs.tax;

import com.openlifeops.core.domain.Action;
import com.openlifeops.core.knowledge.KnowledgeHit;
import com.openlifeops.knowledge.KnowledgeService;
import com.openlifeops.mcp.ToolInvocationResult;
import com.openlifeops.mcp.ToolRegistry;

import java.util.List;
import java.util.Map;

/** Tax-pack behavior layered over a generic ToolRegistry. */
public final class TaxReconciliationToolRegistry implements ToolRegistry {

    private final ToolRegistry delegate;
    private final KnowledgeService knowledgeService;
    private final TaxDocumentFactsExtractor extractor = new TaxDocumentFactsExtractor();
    private final TaxReconciler reconciler = new TaxReconciler();

    public TaxReconciliationToolRegistry(ToolRegistry delegate, KnowledgeService knowledgeService) {
        this.delegate = delegate;
        this.knowledgeService = knowledgeService;
    }

    @Override
    public ToolInvocationResult invoke(String packId, String objective, Action action) {
        if (!TaxPack.PACK_ID.equals(packId)) {
            return delegate.invoke(packId, objective, action);
        }
        if ("tax.reconcile".equals(action.getToolId())) {
            TaxReconciliationReport report = reconciliationReport();
            return reportResult(report, "tax.reconcile");
        }
        if ("tax.submit_report".equals(action.getToolId())) {
            TaxReconciliationReport report = reconciliationReport();
            return new ToolInvocationResult(
                    "{\"submittedForReview\":true,\"report\":" + TaxReportJson.report(report) + "}",
                    "tax.reconciliation.review",
                    0L,
                    reportMetadata(report, "tax.submit_report"));
        }
        return delegate.invoke(packId, objective, action);
    }

    private TaxReconciliationReport reconciliationReport() {
        List<TaxDocumentFacts> facts = knowledgeService.allChunks(TaxPack.PACK_ID).stream()
                .map(extractor::extract)
                .toList();
        return reconciler.reconcile(facts);
    }

    private static ToolInvocationResult reportResult(TaxReconciliationReport report, String toolId) {
        return new ToolInvocationResult(
                TaxReportJson.report(report),
                "tax.reconciliation",
                0L,
                reportMetadata(report, toolId));
    }

    private static Map<String, String> reportMetadata(TaxReconciliationReport report, String toolId) {
        return Map.of(
                "artifactType", "TaxReconciliationReport",
                "reportId", report.id(),
                "toolId", toolId,
                "citations", TaxReportJson.citations(report),
                "reportStatus", report.hasMismatches() ? "REVIEW_REQUIRED" : "RECONCILED");
    }
}
