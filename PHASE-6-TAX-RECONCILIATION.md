# OpenLifeOps Phase 6 — Deterministic Tax Reconciliation

## Status: complete (deterministic, in-memory)

The Tax reference pack now performs a deterministic reconciliation over ingested text instead of returning canned ledger JSON.

```text
Ingest Form 16 / statement text
  → extract PAN, financial year, employer, income, TDS
  → compare documents
  → TaxReconciliationReport + cited findings
  → human approval
  → submit report to OpenLifeOps Tax Review Queue
```

## Scope

- Text and text-based PDF input are supported. OCR and actual tax-portal integrations are intentionally out of scope.
- Rules extract PAN, financial year, employer, income, and TDS with deterministic patterns.
- Findings are `MATCHED`, `MISMATCH`, `MISSING_FIELD`, or `MISSING_DOCUMENT`.
- A report with a mismatch has `reportStatus: REVIEW_REQUIRED`; it does **not** block creation of the review package.
- The high-risk final action goes only to **OpenLifeOps Tax Review Queue**. It never files an ITR, sends an email, or changes a government record.

## Evidence and report artifact

The `tax.reconcile` evidence record contains:

- `metadata.artifactType = TaxReconciliationReport`
- `metadata.reportId`
- `metadata.reportStatus`
- `metadata.citations` — JSON list of `documentId`, `chunkId`, filename, and excerpt for each source used by the report.

The report JSON is retained as the immutable action observation/evidence output in the current in-memory runtime.

## Verified cases

- Matching documents produce a reconciled report.
- Different TDS amounts produce `MISMATCH` / `REVIEW_REQUIRED`.
- Missing required Form 16 fields produce `MISSING_FIELD` findings.
- The task pauses before submitting its internal review package.

## Deliberately deferred

- OCR ingestion for scanned PDFs
- ITR computation/filing or Income Tax Portal integration
- Spring AI reasoning
- Durable persistence; keep validating the in-memory workflow first.
