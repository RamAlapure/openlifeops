package com.openlifeops.api;

import com.openlifeops.api.support.TaxMcpServerProcess;
import com.openlifeops.core.domain.ExecutionStatus;
import com.openlifeops.core.domain.TaskStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mcp")
class TaskMcpIntegrationTest {

    private static final TaxMcpServerProcess TAX_MCP_SERVER = startTaxMcpServer();

    @Autowired
    private MockMvc mockMvc;

    private static TaxMcpServerProcess startTaxMcpServer() {
        try {
            return TaxMcpServerProcess.start();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to start the tax MCP server for integration testing", exception);
        }
    }

    @AfterAll
    static void stopTaxMcpServer() {
        TAX_MCP_SERVER.close();
    }

    @DynamicPropertySource
    static void configureMcpClient(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.ai.mcp.client.streamable-http.connections.tax-tools.url",
                TAX_MCP_SERVER::baseUrl);
    }

    @Test
    void listsTaxToolsFromMcpServer() throws Exception {
        mockMvc.perform(get("/api/v1/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].toolName", org.hamcrest.Matchers.hasItems(
                        "tax_read_document", "tax_reconcile", "tax_submit_report")));
    }

    @Test
    void taxTaskUsesMcpObservationsAndEvidence() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "objective": "Reconcile my tax documents",
                                  "pack": "tax"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionStatus").value(ExecutionStatus.AWAITING_APPROVAL.name()))
                .andReturn();

        String taskId = extractJsonValue(createResult.getResponse().getContentAsString(), "taskId");

        MvcResult detail = mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observations", hasSize(2)))
                .andExpect(jsonPath("$.observations[*].output", hasItem(containsString("income"))))
                .andExpect(jsonPath("$.observations[*].output", not(hasItem(containsString("stub-result")))))
                .andExpect(jsonPath("$.evidence[0].source", containsString("tool:tax")))
                .andReturn();

        String actionId = extractJsonValue(detail.getResponse().getContentAsString(), "pendingActionId");

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
                .andExpect(jsonPath("$.taskStatus").value(TaskStatus.COMPLETED.name()));

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observations", hasSize(3)))
                .andExpect(jsonPath("$.observations[*].output", hasItem(containsString("submitted"))));
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
}
