package com.openlifeops.packs.tax;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Immutable report artifact represented in the execution observation and evidence. */
public record TaxReconciliationReport(
        String id,
        Instant createdAt,
        List<TaxDocumentFacts> documents,
        List<TaxReconciliationFinding> findings) {

    public TaxReconciliationReport {
        documents = List.copyOf(documents);
        findings = List.copyOf(findings);
    }

    public static TaxReconciliationReport create(
            List<TaxDocumentFacts> documents, List<TaxReconciliationFinding> findings) {
        return new TaxReconciliationReport(UUID.randomUUID().toString(), Instant.now(), documents, findings);
    }

    public boolean hasMismatches() {
        return findings.stream().anyMatch(finding -> finding.type() == TaxFindingType.MISMATCH);
    }
}
