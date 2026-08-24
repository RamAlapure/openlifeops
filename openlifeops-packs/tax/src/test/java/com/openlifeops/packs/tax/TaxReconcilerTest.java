package com.openlifeops.packs.tax;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaxReconcilerTest {

    private final TaxReconciler reconciler = new TaxReconciler();

    @Test
    void reportsCleanReconciliationForMatchingDocuments() {
        TaxReconciliationReport report = reconciler.reconcile(List.of(
                facts("form16.txt", 1_200_000L, 245_000L),
                facts("statement.txt", 1_200_000L, 245_000L)));

        assertFalse(report.hasMismatches());
        assertTrue(report.findings().stream().anyMatch(finding -> finding.type() == TaxFindingType.MATCHED));
    }

    @Test
    void reportsMismatchWhenTdsDiffers() {
        TaxReconciliationReport report = reconciler.reconcile(List.of(
                facts("form16.txt", 1_200_000L, 245_000L),
                facts("statement.txt", 1_200_000L, 240_000L)));

        assertTrue(report.hasMismatches());
        assertTrue(report.findings().stream()
                .anyMatch(finding -> finding.type() == TaxFindingType.MISMATCH && finding.field().equals("TDS")));
    }

    @Test
    void reportsMissingRequiredField() {
        TaxDocumentFacts incomplete = new TaxDocumentFacts(
                "doc-1", "chunk-1", "form16.txt", "excerpt", "ABCDE1234F", "2025-26", "Acme", 1_200_000L, null);

        TaxReconciliationReport report = reconciler.reconcile(List.of(incomplete));

        assertTrue(report.findings().stream()
                .anyMatch(finding -> finding.type() == TaxFindingType.MISSING_FIELD && finding.field().equals("TDS")));
    }

    private static TaxDocumentFacts facts(String fileName, long income, long tds) {
        return new TaxDocumentFacts(
                fileName + "-document", fileName + "-chunk", fileName, "excerpt",
                "ABCDE1234F", "2025-26", "Acme", income, tds);
    }
}
