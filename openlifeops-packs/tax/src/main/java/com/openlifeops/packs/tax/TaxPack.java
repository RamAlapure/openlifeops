package com.openlifeops.packs.tax;

import com.openlifeops.core.descriptor.PolicyDefinition;
import com.openlifeops.core.descriptor.ToolDescriptor;
import com.openlifeops.core.descriptor.WorkflowDefinition;
import com.openlifeops.core.domain.ActionType;
import com.openlifeops.core.domain.RiskLevel;
import com.openlifeops.core.mcp.McpToolReference;
import com.openlifeops.core.pack.OpenLifeOpsPack;
import com.openlifeops.core.workflow.WorkflowStepTemplate;

import java.util.List;
import java.util.Map;

public final class TaxPack implements OpenLifeOpsPack {

    public static final String PACK_ID = "tax";
    public static final String WORKFLOW_RECONCILE = "tax.reconcile.documents";
    public static final String WORKFLOW_VERSION = "2";

    @Override
    public String id() {
        return PACK_ID;
    }

    @Override
    public String name() {
        return "Tax Pack";
    }

    @Override
    public List<WorkflowDefinition> workflows() {
        return List.of(new WorkflowDefinition(
                WORKFLOW_RECONCILE,
                "Tax document reconciliation",
                "Reconcile uploaded tax documents and produce an evidence-backed report"));
    }

    @Override
    public List<ToolDescriptor> tools() {
        return List.of(
                new ToolDescriptor("tax.read_document", "Read tax document", "Extract fields from Form 16 and statements"),
                new ToolDescriptor("tax.reconcile", "Reconcile tax ledger", "Cross-check income, TDS, and capital gains"),
                new ToolDescriptor("tax.submit_report", "Submit reconciliation report", "Submit the internal reconciliation report for human review"));
    }

    @Override
    public Map<String, McpToolReference> mcpToolBindings() {
        return Map.of(
                "tax.read_document", new McpToolReference("tax-tools", "tax_read_document"),
                "tax.reconcile", new McpToolReference("tax-tools", "tax_reconcile"),
                "tax.submit_report", new McpToolReference("tax-tools", "tax_submit_report"));
    }

    @Override
    public List<PolicyDefinition> policies() {
        return List.of(
                new PolicyDefinition("document_read", ActionType.READ_DOCUMENT, RiskLevel.LOW, false),
                new PolicyDefinition("tax_calculate", ActionType.CALCULATE, RiskLevel.MEDIUM, false),
                new PolicyDefinition("government_submission", ActionType.SUBMIT_DOCUMENT, RiskLevel.HIGH, true));
    }

    @Override
    public String workflowVersion(String workflowId) {
        if (WORKFLOW_RECONCILE.equals(workflowId)) {
            return WORKFLOW_VERSION;
        }
        return OpenLifeOpsPack.super.workflowVersion(workflowId);
    }

    @Override
    public List<WorkflowStepTemplate> workflowSteps(String workflowId) {
        if (!WORKFLOW_RECONCILE.equals(workflowId)) {
            return List.of();
        }
        return List.of(
                new WorkflowStepTemplate(
                        "read_documents",
                        "Read documents",
                        1,
                        ActionType.READ_DOCUMENT,
                        "uploaded-documents",
                        RiskLevel.LOW,
                        "tax.read_document"),
                new WorkflowStepTemplate(
                        "reconcile_ledger",
                        "Reconcile ledger",
                        2,
                        ActionType.CALCULATE,
                        "tax-ledger",
                        RiskLevel.MEDIUM,
                        "tax.reconcile"),
                new WorkflowStepTemplate(
                        "submit_report",
                        "Submit reconciliation",
                        3,
                        ActionType.SUBMIT_DOCUMENT,
                        "OpenLifeOps Tax Review Queue",
                        RiskLevel.HIGH,
                        "tax.submit_report"));
    }
}
