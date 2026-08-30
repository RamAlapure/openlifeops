# OpenLifeOps Runtime

Open operating system for AI-powered life workflows.

> **Plan. Act. Verify. Ask when it matters.**

**Stack:** Java 21 (bytecode; run on JDK 21 or 25), Spring Boot 4.1.0, Spring AI MCP client (Phase 3), modular Maven monolith.

**Note:** Eliza4J is BNY-internal; OpenLifeOps uses Spring AI directly for reasoning (Phase 5).

## Phase 2 demo (stub tools, default profile)

```bash
mvnw.cmd verify
mvnw.cmd -pl openlifeops-api spring-boot:run
```

Create a tax task (3-step pack-driven workflow):

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{"objective":"Reconcile my tax documents","pack":"tax"}'
```

Response: `taskStatus=ACTIVE`, `executionStatus=AWAITING_APPROVAL`. Get task for `pendingActionId`, then approve:

```bash
curl -X POST http://localhost:8080/api/v1/tasks/{taskId}/approvals \
  -H "Content-Type: application/json" \
  -d '{"actionId":"{actionId}","decision":"APPROVED","decidedBy":"user","comment":"Looks good"}'
```

Retry after failure:

```bash
curl -X POST http://localhost:8080/api/v1/tasks/{taskId}/retry
```

Persistence: `in-memory` profile (default for dev/tests). Mongo profile placeholder — run `docker-compose up -d` when Mongo adapters land.

## Phase 3 demo (MCP tools — Streamable HTTP)

**Terminal 1** — tax MCP server:

```bash
mvnw.cmd -pl openlifeops-tax-mcp-server spring-boot:run
```

**Terminal 2** — OpenLifeOps API (`mcp` profile connects to `http://localhost:8090/mcp`):

```bash
SPRING_PROFILES_ACTIVE=mcp mvnw.cmd -pl openlifeops-api spring-boot:run
```

List discovered MCP tools: `GET /api/v1/tools`. Task observations use MCP JSON payloads; evidence provenance is `mcp:tax-tools`. See [help.md](help.md) and [PHASE-3-MCP.md](PHASE-3-MCP.md).

## Modules

- `openlifeops-core` — domain spine, Plan, Observation, events, pack contract
- `openlifeops-runtime` — Planner, ExecutionEngine, Executor, Validator, TaskManager
- `openlifeops-orchestrator` — `OpenLifeOps.execute(TaskRequest)`
- `openlifeops-governance` — `PolicyEngine.evaluate(Action)`
- `openlifeops-evidence` — append-only evidence store
- `openlifeops-mcp` — `ToolRegistry`, stub + MCP invocation contracts
- `openlifeops-tax-mcp-server` — Spring AI Streamable HTTP MCP server for the tax pack
- `openlifeops-packs/tax` - Tax 3-step workflow (`workflowVersion=2`)
- `openlifeops-api` — Spring Boot REST API

See [PHASE-2-RUNTIME.md](PHASE-2-RUNTIME.md) for Phase 2 architecture. Phase 3 MCP: [PHASE-3-MCP.md](PHASE-3-MCP.md).
Phase 4 knowledge and cited evidence with vector embeddings: [PHASE-4-KNOWLEDGE.md](PHASE-4-KNOWLEDGE.md).
Phase 6 deterministic Tax reconciliation: [PHASE-6-TAX-RECONCILIATION.md](PHASE-6-TAX-RECONCILIATION.md).
Phase 7 PDF/text document ingestion: [PHASE-7-DOCUMENT-INGESTION.md](PHASE-7-DOCUMENT-INGESTION.md).
Phase 8 optional Spring AI review: [PHASE-8-AI-REVIEW.md](PHASE-8-AI-REVIEW.md).

**Manual testing:** [help.md](help.md)

The optional AI review endpoint uses the deterministic fallback unless a Spring AI `ChatClient.Builder` is available from a configured model provider.
