package com.openlifeops.packs.tax;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class TaxReconciler {

    public TaxReconciliationReport reconcile(List<TaxDocumentFacts> documents) {
        List<TaxReconciliationFinding> findings = new ArrayList<>();
        if (documents.isEmpty()) {
            findings.add(new TaxReconciliationFinding(
                    TaxFindingType.MISSING_DOCUMENT,
                    "documents",
                    "No tax documents were ingested for this task.",
                    List.of()));
            return TaxReconciliationReport.create(documents, findings);
        }

        for (TaxDocumentFacts document : documents) {
            missingIfNull(findings, document, "PAN", document.pan());
            missingIfNull(findings, document, "financialYear", document.financialYear());
            missingIfNull(findings, document, "income", document.income());
            missingIfNull(findings, document, "TDS", document.tds());
        }
        reconcileField(findings, documents, "PAN", TaxDocumentFacts::pan);
        reconcileField(findings, documents, "financialYear", TaxDocumentFacts::financialYear);
        reconcileField(findings, documents, "income", facts -> facts.income() == null ? null : facts.income().toString());
        reconcileField(findings, documents, "TDS", facts -> facts.tds() == null ? null : facts.tds().toString());
        return TaxReconciliationReport.create(documents, findings);
    }

    private static void missingIfNull(
            List<TaxReconciliationFinding> findings, TaxDocumentFacts document, String field, Object value) {
        if (value == null) {
            findings.add(new TaxReconciliationFinding(
                    TaxFindingType.MISSING_FIELD,
                    field,
                    "Missing " + field + " in " + document.fileName() + ".",
                    List.of(document)));
        }
    }

    private static void reconcileField(
            List<TaxReconciliationFinding> findings,
            List<TaxDocumentFacts> documents,
            String field,
            Function<TaxDocumentFacts, String> valueExtractor) {
        List<TaxDocumentFacts> present = documents.stream()
                .filter(document -> valueExtractor.apply(document) != null)
                .toList();
        if (present.isEmpty()) {
            return;
        }
        long distinctValues = present.stream().map(valueExtractor).distinct().count();
        if (distinctValues > 1) {
            findings.add(new TaxReconciliationFinding(
                    TaxFindingType.MISMATCH,
                    field,
                    "Conflicting " + field + " values across tax documents.",
                    present));
        } else {
            findings.add(new TaxReconciliationFinding(
                    TaxFindingType.MATCHED,
                    field,
                    field + " is consistent across " + present.size() + " document(s).",
                    present));
        }
    }
}
