package com.openlifeops.packs.tax;

import java.util.List;
import java.util.stream.Collectors;

final class TaxReportJson {

    private TaxReportJson() {
    }

    static String report(TaxReconciliationReport report) {
        return "{" +
                "\"reportId\":\"" + escape(report.id()) + "\"," +
                "\"createdAt\":\"" + report.createdAt() + "\"," +
                "\"status\":\"" + (report.hasMismatches() ? "REVIEW_REQUIRED" : "RECONCILED") + "\"," +
                "\"findings\":" + findings(report.findings()) +
                "}";
    }

    static String citations(TaxReconciliationReport report) {
        return report.findings().stream()
                .flatMap(finding -> finding.sources().stream())
                .distinct()
                .map(source -> "{\"documentId\":\"" + escape(source.documentId())
                        + "\",\"chunkId\":\"" + escape(source.chunkId())
                        + "\",\"fileName\":\"" + escape(source.fileName())
                        + "\",\"excerpt\":\"" + escape(source.excerpt()) + "\"}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String findings(List<TaxReconciliationFinding> findings) {
        return findings.stream()
                .map(finding -> "{\"type\":\"" + finding.type() + "\",\"field\":\""
                        + escape(finding.field()) + "\",\"message\":\"" + escape(finding.message()) + "\"}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
