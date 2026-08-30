package com.openlifeops.api;

import com.openlifeops.core.domain.ApprovalDecision;
import com.openlifeops.core.domain.ExecutionStatus;
import com.openlifeops.core.domain.TaskStatus;
import com.openlifeops.api.web.InMemoryUploadedDocumentStore;
import org.junit.jupiter.api.Test;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
class TaskFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryUploadedDocumentStore uploadedDocumentStore;

    @Test
    void pdfUploadExtractsTextIndexesItAndRetainsSourceBytesInMemory() throws Exception {
        byte[] pdf = pdfWithText("""
                Form 16
                PAN: ABCDE1234F
                Financial Year: 2025-26
                Employer: Example Services Ltd
                Income from salary: 1200000
                TDS deducted: 245000
                """);
        MockMultipartFile file = new MockMultipartFile("file", "form16-example.pdf", "application/pdf", pdf);

        MvcResult upload = mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .param("pack", "tax")
                        .param("documentType", "FORM_16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("form16-example.pdf"))
                .andExpect(jsonPath("$.contentType").value("text/plain"))
                .andExpect(jsonPath("$.attributes.ingestionMethod").value("multipart-upload"))
                .andExpect(jsonPath("$.attributes.sourceContentType").value("application/pdf"))
                .andExpect(jsonPath("$.attributes.extractionStatus").value("SUCCESS"))
                .andReturn();
        String documentId = extractJsonValue(upload.getResponse().getContentAsString(), "id");

        org.junit.jupiter.api.Assertions.assertArrayEquals(
                pdf, uploadedDocumentStore.find(documentId).orElseThrow().bytes());

        mockMvc.perform(get("/api/v1/documents/search")
                        .queryParam("pack", "tax")
                        .queryParam("query", "Form 16 income TDS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].documentId", hasItem(documentId)));

        MvcResult task = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" 
                                {"objective":"Reconcile uploaded Form 16","pack":"tax"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String taskId = extractJsonValue(task.getResponse().getContentAsString(), "taskId");

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidence[*].metadata.citations", hasItem(containsString(documentId))));
    }

    @Test
    void uploadRejectsFileClaimingToBePdfWithoutPdfContent() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "not-a-pdf.pdf", "application/pdf", "not a PDF".getBytes());

        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .param("pack", "tax")
                        .param("documentType", "FORM_16"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error", containsString("valid PDF header")));
    }

    @Test
    void taxTaskThreeStepsRequiresApprovalThenCompletes() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "objective": "Reconcile my tax documents",
                                  "pack": "tax"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.executionStatus").value(ExecutionStatus.AWAITING_APPROVAL.name()))
                .andReturn();

        String taskId = extractJsonValue(createResult.getResponse().getContentAsString(), "taskId");
        String actionId = extractJsonValue(createResult.getResponse().getContentAsString(), "pendingActionId");

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps", hasSize(3)))
                .andExpect(jsonPath("$.evidence", hasSize(2)))
                .andExpect(jsonPath("$.observations", hasSize(2)))
                .andExpect(jsonPath("$.workflowVersion").value("2"));

        if (actionId == null || actionId.isBlank()) {
            MvcResult detail = mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)).andReturn();
            actionId = extractJsonValue(detail.getResponse().getContentAsString(), "pendingActionId");
        }

        mockMvc.perform(post("/api/v1/tasks/{taskId}/approvals", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actionId": "%s",
                                  "decision": "APPROVED",
                                  "decidedBy": "user",
                                  "comment": "Looks good"
                                }
                                """.formatted(actionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.executionStatus").value(ExecutionStatus.COMPLETED.name()));

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidence", hasSize(3)))
                .andExpect(jsonPath("$.observations", hasSize(3)));

        mockMvc.perform(post("/api/v1/tasks/{taskId}/approvals", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actionId": "%s",
                                  "decision": "APPROVED",
                                  "decidedBy": "user",
                                  "comment": "Duplicate"
                                }
                                """.formatted(actionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Approval already processed"));

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidence", hasSize(3)))
                .andExpect(jsonPath("$.observations", hasSize(3)));
    }

    @Test
    void ingestedTaxDocumentProducesCitedReadEvidence() throws Exception {
        MvcResult ingestResult = mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pack": "tax",
                                  "documentType": "FORM_16",
                                  "fileName": "form16-fy2025.txt",
                                  "content": "Form 16 tax certificate. Income from salary is 1200000. TDS deducted is 245000.",
                                  "attributes": {"taxYear": "2025-26"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pack").value("tax"))
                .andReturn();
        String documentId = extractJsonValue(ingestResult.getResponse().getContentAsString(), "id");

        mockMvc.perform(get("/api/v1/documents/search")
                        .queryParam("pack", "tax")
                        .queryParam("query", "Form 16 income TDS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].documentId", hasItem(documentId)));

        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "objective": "Reconcile my Form 16 tax documents and TDS",
                                  "pack": "tax"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionStatus").value(ExecutionStatus.AWAITING_APPROVAL.name()))
                .andReturn();
        String taskId = extractJsonValue(createResult.getResponse().getContentAsString(), "taskId");

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidence[*].source", hasItem(startsWith("knowledge:"))))
                .andExpect(jsonPath("$.evidence[*].metadata.excerpt", hasItem(containsString("Income from salary"))));
    }

    @Test
    void approvalWithoutDecisionReturnsBadRequest() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"objective":"Reconcile my tax documents","pack":"tax"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String taskId = extractJsonValue(createResult.getResponse().getContentAsString(), "taskId");
        MvcResult detail = mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)).andReturn();
        String actionId = extractJsonValue(detail.getResponse().getContentAsString(), "pendingActionId");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/approvals", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actionId":"%s"}
                                """.formatted(actionId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reviewSummaryUsesSafeDeterministicFallbackWhenNoChatModelIsConfigured() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"objective":"Review my tax reconciliation","pack":"tax"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String taskId = extractJsonValue(createResult.getResponse().getContentAsString(), "taskId");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/review-summary", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId))
                .andExpect(jsonPath("$.reportId", startsWith("")))
                .andExpect(jsonPath("$.provider").value("deterministic"))
                .andExpect(jsonPath("$.summary", containsString("deterministic reconciliation")))
                .andExpect(jsonPath("$.citationIds").isArray());
    }

    @Test
    void taxTaskProducesCitedMismatchReportBeforeApproval() throws Exception {
        ingestTaxText("form16.txt", """
                Form 16. PAN: ABCDE1234F. Financial Year: 2025-26.
                Employer: Acme Ltd. Income from salary: 1200000. TDS deducted: 245000.
                """);
        ingestTaxText("bank-statement.txt", """
                PAN: ABCDE1234F. FY: 2025-26.
                Income from salary: 1200000. TDS: 240000.
                """);

        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"objective":"Reconcile Form 16 and TDS","pack":"tax"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionStatus").value(ExecutionStatus.AWAITING_APPROVAL.name()))
                .andReturn();
        String taskId = extractJsonValue(createResult.getResponse().getContentAsString(), "taskId");

        MvcResult detail = mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidence[*].metadata.artifactType", hasItem("TaxReconciliationReport")))
                .andExpect(jsonPath("$.evidence[*].metadata.reportStatus", hasItem("REVIEW_REQUIRED")))
                .andExpect(jsonPath("$.evidence[*].metadata.citations", hasItem(containsString("form16.txt"))))
                .andExpect(jsonPath("$.evidence[*].extractedValue", hasItem(containsString("MISMATCH"))))
                .andReturn();
        String actionId = extractJsonValue(detail.getResponse().getContentAsString(), "pendingActionId");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/approvals", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actionId":"%s","decision":"APPROVED","decidedBy":"user"}
                                """.formatted(actionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.COMPLETED.name()));
    }

    private void ingestTaxText(String fileName, String content) throws Exception {
        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pack":"tax","documentType":"FORM_16","fileName":"%s","content":%s}
                                """.formatted(fileName, jsonString(content))))
                .andExpect(status().isOk());
    }

    private static String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    @Test
    void failedReconcileCanRetryAndComplete() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "objective": "[fail-reconcile] Reconcile my tax documents",
                                  "pack": "tax"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionStatus").value(ExecutionStatus.FAILED.name()))
                .andReturn();

        String taskId = extractJsonValue(createResult.getResponse().getContentAsString(), "taskId");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/retry", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionStatus").value(ExecutionStatus.AWAITING_APPROVAL.name()));

        MvcResult detail = mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)).andReturn();
        String actionId = extractJsonValue(detail.getResponse().getContentAsString(), "pendingActionId");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/approvals", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actionId": "%s",
                                  "decision": "APPROVED",
                                  "decidedBy": "user"
                                }
                                """.formatted(actionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.COMPLETED.name()));
    }

    private String extractJsonValue(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    private static byte[] pdfWithText(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.setLeading(16);
                content.newLineAtOffset(72, 720);
                for (String line : text.lines().toList()) {
                    content.showText(line);
                    content.newLine();
                }
                content.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
