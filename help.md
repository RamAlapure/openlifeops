# OpenLifeOps — Manual testing guide

Step-by-step instructions to run and exercise the API by hand. For architecture and phase scope, see [README.md](README.md) and [PHASE-2-RUNTIME.md](PHASE-2-RUNTIME.md).

**Base URL:** `http://localhost:8080`

---

## Prerequisites

| Requirement | Notes |
| --- | --- |
| JDK 21 or 25 | `java -version` |
| Maven | Use `mvnw.cmd` in this folder, or IntelliJ-bundled Maven |
| curl | Windows 10+ includes curl; or use PowerShell examples below |

No MongoDB required for manual testing — the default **`in-memory`** profile stores everything in process memory.

---

## 1. Build and run automated tests

From the `openlifeops` directory:

```powershell
cd c:\Users\alapu\Workspace\ai-projects\openlifeops
.\mvnw.cmd verify
```

All modules compile and integration tests run on the `in-memory` profile. Fix any failures before manual testing.

---

## 2. Start the API

```powershell
.\mvnw.cmd -pl openlifeops-api spring-boot:run
```

Wait for Spring Boot to finish starting. You should see:

```text
:: Spring Boot ::  (v4.1.0)
```

The server listens on **port 8080**. Leave this terminal open.

### MCP profile (Phase 3 — real tool execution)

Phase 3 routes tool calls through a **Spring AI Streamable HTTP MCP server** instead of in-memory stubs.

**Start the tax MCP server** (Terminal 1):

```powershell
cd c:\Users\alapu\Workspace\ai-projects\openlifeops
.\mvnw.cmd -pl openlifeops-tax-mcp-server spring-boot:run
```

Server: **http://localhost:8090** — MCP endpoint **/mcp** (Streamable HTTP).

**Start the OpenLifeOps API with the `mcp` profile** (Terminal 2):

```powershell
cd c:\Users\alapu\Workspace\ai-projects\openlifeops
$env:SPRING_PROFILES_ACTIVE = "mcp"
.\mvnw.cmd -pl openlifeops-api spring-boot:run
```

Verify tool discovery:

```powershell
curl -s http://localhost:8080/api/v1/tools
```

You should see three tools: `tax_read_document`, `tax_reconcile`, `tax_submit_report`.

Then run sections 4–5 below. Observations will contain **JSON from MCP** (e.g. `"income":1200000`) instead of `stub-result:...`. Evidence `provenance` will be `mcp:tax-tools`.

To return to stub tools, omit the profile or use `SPRING_PROFILES_ACTIVE=in-memory`.

---

## 3. What the Tax pack does

Creating a task with `pack: "tax"` runs a **3-step workflow** (`tax.reconcile.documents`, version `2`):

| Step | Action | Risk | Runtime behavior |
| --- | --- | --- | --- |
| 1 | Read documents | LOW | Runs automatically |
| 2 | Reconcile ledger | MEDIUM | Runs automatically + validation |
| 3 | Submit reconciliation | HIGH | **Pauses for human approval** |

After step 3 is approved, the task completes. Each executed step produces one **Observation** and one **Evidence** record.

---

## 3.1 Ingest source text and verify a citation

Use JSON ingestion for quick text fixtures (for example, extracted Form 16 text):

```powershell
$document = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/v1/documents" `
  -ContentType "application/json" `
  -Body (@{
    pack = "tax"
    documentType = "FORM_16"
    fileName = "form16-fy2025.txt"
    content = "Form 16 tax certificate. Income from salary is 1200000. TDS deducted is 245000."
    attributes = @{ taxYear = "2025-26" }
  } | ConvertTo-Json -Depth 3)

$document
```

Search the indexed chunks:

```powershell
curl -s "http://localhost:8080/api/v1/documents/search?pack=tax&query=Form%2016%20income%20TDS"
```

Now create a Tax task whose objective mentions the document terms. In `GET /api/v1/tasks/{taskId}`, the read-step evidence will have:

```json
{
  "source": "knowledge:{documentId}",
  "provenance": "knowledge:{documentId}",
  "metadata": {
    "documentId": "...",
    "chunkId": "...",
    "excerpt": "Form 16 tax certificate..."
  }
}
```

The remaining Tax steps continue to use the configured stub or MCP tool. See [PHASE-4-KNOWLEDGE.md](PHASE-4-KNOWLEDGE.md) for the knowledge baseline.

---

## 3.2 Upload a real text-based PDF (Phase 7)

The upload endpoint accepts `application/pdf` documents with embedded text and `text/plain` files. It keeps source bytes only in the current process, extracts text, and indexes that text for the existing Tax workflow.

```powershell
curl.exe -s -X POST "http://localhost:8080/api/v1/documents/upload" `
  -F "pack=tax" `
  -F "documentType=FORM_16" `
  -F "file=@C:\temp\form16.pdf;type=application/pdf"
```

The response includes extraction metadata under `attributes`:

```json
{
  "fileName": "form16.pdf",
  "contentType": "text/plain",
  "attributes": {
    "ingestionMethod": "multipart-upload",
    "sourceContentType": "application/pdf",
    "sourceBytes": "...",
    "extractedCharacters": "...",
    "extractionStatus": "SUCCESS"
  }
}
```

Then create the Tax task as usual. Its reconciliation report cites the uploaded document through the normal evidence metadata. Uploads are limited to **10 MB**. Password-protected, corrupt, and image-only/scanned PDFs are rejected; OCR is not available yet.

---

## 3.3 Phase 6 - deterministic Tax reconciliation

Ingest at least two tax texts before creating the task. The reconciler extracts these fields where present:

- PAN
- financial year / FY
- employer
- income from salary / gross salary / taxable income
- TDS

For a mismatch demo, ingest two documents with the same PAN and income but different TDS amounts:

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/v1/documents" `
  -ContentType "application/json" `
  -Body (@{
    pack = "tax"; documentType = "FORM_16"; fileName = "form16.txt"
    content = "Form 16. PAN: ABCDE1234F. Financial Year: 2025-26. Employer: Acme Ltd. Income from salary: 1200000. TDS deducted: 245000."
  } | ConvertTo-Json)

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/v1/documents" `
  -ContentType "application/json" `
  -Body (@{
    pack = "tax"; documentType = "STATEMENT"; fileName = "statement.txt"
    content = "PAN: ABCDE1234F. FY: 2025-26. Income from salary: 1200000. TDS: 240000."
  } | ConvertTo-Json)
```

Create the task as usual, then use `GET /api/v1/tasks/{taskId}`. The **Reconcile ledger** evidence contains a JSON `TaxReconciliationReport`:

```json
{
  "status": "REVIEW_REQUIRED",
  "findings": [
    { "type": "MISMATCH", "field": "TDS" }
  ]
}
```

Its metadata holds citations for every source document/chunk. Approval only submits this internal report to the **OpenLifeOps Tax Review Queue**; it does not file anything with a government portal. See [PHASE-6-TAX-RECONCILIATION.md](PHASE-6-TAX-RECONCILIATION.md).

---

## 4. Happy path — create, inspect, approve

### 4.1 Create a tax task

**curl (PowerShell):**

```powershell
curl -s -X POST http://localhost:8080/api/v1/tasks `
  -H "Content-Type: application/json" `
  -d "{\"objective\":\"Reconcile my tax documents\",\"pack\":\"tax\"}"
```

**PowerShell alternative:**

```powershell
$body = @{ objective = "Reconcile my tax documents"; pack = "tax" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/tasks" -ContentType "application/json" -Body $body
```

**Expected response:**

```json
{
  "taskId": "...",
  "executionId": "...",
  "taskStatus": "ACTIVE",
  "executionStatus": "AWAITING_APPROVAL",
  "message": "Awaiting human approval"
}
```

Copy `taskId` for the next steps.

### 4.2 Inspect task state

```powershell
curl -s http://localhost:8080/api/v1/tasks/{taskId}
```

Replace `{taskId}` with the id from step 4.1.

**Check:**

- `taskStatus` = `ACTIVE`
- `executionStatus` = `AWAITING_APPROVAL`
- `workflowVersion` = `1`
- `steps` has **3** entries; first two `status` = `COMPLETED`, third often `RUNNING` or pending
- `evidence` and `observations` each have **2** entries (steps 1 and 2 completed)
- `pendingActionId` is set — copy this for approval

Example step keys: `read_documents`, `reconcile_ledger`, `submit_report`.

### 4.3 Approve the submit action

Approval must target the **specific action** (`actionId`), not just the task.

```powershell
curl -s -X POST "http://localhost:8080/api/v1/tasks/{taskId}/approvals" `
  -H "Content-Type: application/json" `
  -d "{\"actionId\":\"{actionId}\",\"decision\":\"APPROVED\",\"decidedBy\":\"user\",\"comment\":\"Looks good\"}"
```

**Expected response:**

```json
{
  "taskStatus": "COMPLETED",
  "executionStatus": "COMPLETED",
  "message": "Task completed"
}
```

### 4.4 Confirm final state

```powershell
curl -s http://localhost:8080/api/v1/tasks/{taskId}
```

**Check:**

- `taskStatus` = `COMPLETED`
- `executionStatus` = `COMPLETED`
- `evidence` and `observations` each have **3** entries
- All three steps `status` = `COMPLETED`
- `pendingActionId` is null

---

## 5. Failure and retry

Use the objective flag `[fail-reconcile]` to force step 2 to fail on the **first** execution attempt only.

### 5.1 Create a task that fails on reconcile

```powershell
curl -s -X POST http://localhost:8080/api/v1/tasks `
  -H "Content-Type: application/json" `
  -d "{\"objective\":\"[fail-reconcile] Reconcile my tax documents\",\"pack\":\"tax\"}"
```

**Expected:**

- `taskStatus` = `ACTIVE`
- `executionStatus` = `FAILED`
- `message` = `Execution failed`

Save `taskId`.

### 5.2 Retry (new execution attempt)

```powershell
curl -s -X POST "http://localhost:8080/api/v1/tasks/{taskId}/retry"
```

**Expected:**

- New `executionId` (attempt 2)
- `executionStatus` = `AWAITING_APPROVAL` (reconcile succeeds on retry; submit waits for approval)

### 5.3 Get task and note attempt number

```powershell
curl -s http://localhost:8080/api/v1/tasks/{taskId}
```

**Check:** `attemptNumber` = `2`, `pendingActionId` present.

### 5.4 Approve and complete

Use the same approval call as in section 4.3 with the new `pendingActionId`.

---

## 6. Idempotent approval (no double execution)

After a task is **COMPLETED**, sending the same approval again should **not** execute the submit action a second time.

1. Complete a happy-path task (section 4).
2. Call approve again with the same `actionId`:

```powershell
curl -s -X POST "http://localhost:8080/api/v1/tasks/{taskId}/approvals" `
  -H "Content-Type: application/json" `
  -d "{\"actionId\":\"{actionId}\",\"decision\":\"APPROVED\",\"decidedBy\":\"user\",\"comment\":\"Duplicate\"}"
```

**Expected:** HTTP 200, `message` = `Approval already processed`.

3. GET the task again — `evidence` and `observations` counts stay at **3** (not 4).

---

## 7. Reject approval

Create a new task (section 4.1), then reject instead of approve:

```powershell
curl -s -X POST "http://localhost:8080/api/v1/tasks/{taskId}/approvals" `
  -H "Content-Type: application/json" `
  -d "{\"actionId\":\"{actionId}\",\"decision\":\"REJECTED\",\"decidedBy\":\"user\",\"comment\":\"Not ready\"}"
```

**Expected:**

- `taskStatus` = `CANCELLED`
- `executionStatus` = `CANCELLED`
- `message` = `Approval rejected`

---

## 8. Error cases (sanity checks)

| Scenario | Request | Expected |
| --- | --- | --- |
| Unknown pack | `pack: "unknown"` | HTTP 400, pack not found |
| Missing `actionId` on approve | approval body without `actionId` | HTTP 400 (validation) |
| Wrong `actionId` on approve | mismatched action id | HTTP 400 |
| Retry while running | `POST .../retry` on ACTIVE task | HTTP 400, not retryable |
| Unknown task | `GET /api/v1/tasks/bad-id` | HTTP 404 |

Example — bad pack:

```powershell
curl -s -X POST http://localhost:8080/api/v1/tasks `
  -H "Content-Type: application/json" `
  -d "{\"objective\":\"test\",\"pack\":\"unknown\"}"
```

---

## 9. API reference (quick)

| Method | Path | Body |
| --- | --- | --- |
| `POST` | `/api/v1/tasks` | `{ "objective": "...", "pack": "tax" }` |
| `GET` | `/api/v1/tasks/{taskId}` | — |
| `POST` | `/api/v1/tasks/{taskId}/approvals` | `{ "actionId": "...", "decision": "APPROVED" \| "REJECTED", "decidedBy": "user", "comment": "..." }` |
| `POST` | `/api/v1/tasks/{taskId}/retry` | — |

**Task vs execution status**

- `taskStatus`: aggregate over the task (`ACTIVE` while work can continue, `COMPLETED` when done).
- `executionStatus`: one attempt (`RUNNING`, `AWAITING_APPROVAL`, `FAILED`, `COMPLETED`, …).

A failed attempt leaves the task `ACTIVE` so you can `retry` and create a new execution.

---

## 10. Stop the server

In the terminal running Spring Boot, press `Ctrl+C`.

---

## 11. Optional — Mongo profile

Mongo adapters are not fully wired yet. The `mongo` profile currently delegates to in-memory storage. When Mongo persistence lands:

```powershell
docker compose up -d
$env:SPRING_PROFILES_ACTIVE="mongo"
.\mvnw.cmd -pl openlifeops-api spring-boot:run
```

Restart the API mid-`AWAITING_APPROVAL` and approve after restart to verify persistence.

---

## Troubleshooting

| Problem | What to try |
| --- | --- |
| `Connection refused` on 8080 | API not running — start with `spring-boot:run` |
| `mvnw.cmd` not found | Run commands from the `openlifeops` folder |
| curl JSON errors on Windows | Use PowerShell `Invoke-RestMethod` or escape quotes as shown |
| Port already in use | Stop other process on 8080 or change `server.port` in `application.yml` |
| Tests fail after code changes | `.\mvnw.cmd verify` and fix before manual testing |
