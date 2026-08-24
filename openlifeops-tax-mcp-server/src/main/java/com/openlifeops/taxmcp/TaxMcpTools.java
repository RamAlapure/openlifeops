package com.openlifeops.taxmcp;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class TaxMcpTools {

    @McpTool(
            name = "tax_read_document",
            description = "Read tax document fields from Form 16 and statements")
    public String taxReadDocument(
            @McpToolParam(description = "Document source identifier", required = false) String target,
            @McpToolParam(description = "OpenLifeOps logical tool id", required = false) String toolId) {
        String source = target == null || target.isBlank() ? "unknown" : target;
        return """
                {"source":"%s","income":1200000,"tds":245000,"documentType":"form16"}
                """.formatted(source.replace("\"", "\\\""));
    }

    @McpTool(
            name = "tax_reconcile",
            description = "Reconcile tax ledger against extracted document data")
    public String taxReconcile(
            @McpToolParam(description = "Ledger identifier", required = false) String target,
            @McpToolParam(description = "OpenLifeOps logical tool id", required = false) String toolId) {
        String ledger = target == null || target.isBlank() ? "unknown" : target;
        return """
                {"ledger":"%s","mismatches":0,"status":"ok"}
                """.formatted(ledger.replace("\"", "\\\""));
    }

    @McpTool(
            name = "tax_submit_report",
            description = "Submit reconciliation report for review")
    public String taxSubmitReport(
            @McpToolParam(description = "Submission destination", required = false) String target,
            @McpToolParam(description = "OpenLifeOps logical tool id", required = false) String toolId) {
        String destination = target == null || target.isBlank() ? "unknown" : target;
        return """
                {"destination":"%s","submitted":true,"reference":"TAX-%d"}
                """.formatted(destination.replace("\"", "\\\""), System.currentTimeMillis());
    }
}
