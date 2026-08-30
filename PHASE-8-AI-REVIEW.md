# OpenLifeOps Phase 8 - Optional Spring AI Tax Review

## Status: complete (safe fallback; model adapter ready)

Phase 8 adds an optional explanation layer after deterministic Tax reconciliation:

```text
Tax documents
  -> deterministic extraction and reconciliation
  -> cited TaxReconciliationReport
  -> optional Spring AI review summary
  -> citation allow-list validation
  -> human approval
```

## Design

- `openlifeops-ai` contains the review contract and Spring AI adapter.
- The API selects `DeterministicTaxReviewService` when no `ChatClient.Builder` is configured.
- With a configured Spring AI model, `SpringAiTaxReviewService` uses `ChatClient` structured output and schema validation.
- The model receives report JSON and allowed citation IDs only. It has no tool callbacks and cannot execute actions.
- Task ID, report ID, and report status are restored from trusted runtime data after model output.
- Model citation IDs are filtered against the report evidence allow-list.
- Review summaries are generated on demand and are not persisted.

## Endpoint

```text
POST /api/v1/tasks/{taskId}/review-summary
```

The response contains `taskId`, `reportId`, unchanged `reportStatus`, a summary, concerns, questions, citation IDs, and the provider used (`deterministic` or `spring-ai`).

## Safety boundary

The AI review layer cannot approve tasks, modify evidence, change policy decisions, invoke MCP tools, submit documents, or access external systems. The deterministic report remains the authoritative result.

## Deferred

- Provider-specific model dependency and credentials in the default profile
- Durable review-summary storage
- LLM planning, tool calling, and autonomous action selection
