# OpenLifeOps Phase 4 — Knowledge and Cited Evidence

## Status: complete (in-memory baseline)

Phase 4 grounds the Tax pack in ingested source material instead of only canned tool responses.

```text
POST /api/v1/documents
  → InMemoryKnowledgeService
  → paragraph-aware chunks + keyword retrieval
  → tax.read_document
  → Observation + immutable Evidence with citation metadata
```

## What is implemented

- Pack-scoped text document ingestion and listing: `POST` / `GET /api/v1/documents`
- Retrieval endpoint: `GET /api/v1/documents/search?pack=tax&query=...`
- Immutable knowledge document/chunk/hit domain model in `openlifeops-core`
- In-memory chunk storage and deterministic keyword retrieval in `openlifeops-knowledge`
- `KnowledgeEnhancingToolRegistry`: `tax.read_document` uses the best Tax-document hit; other tools retain their configured stub/MCP path.
- Cited evidence metadata: `documentId`, `chunkId`, `excerpt`, document type, and filename.
- Regression tests proving ingest → retrieve → Tax task → cited evidence.

## Current limitation

This is deliberately a **text ingestion baseline**, not a production RAG implementation:

- No multipart upload, PDF parsing, OCR, embeddings, or pgvector yet.
- Data is process-local under the `in-memory` persistence profile.
- Retrieval is deterministic keyword matching, which keeps Phase 4 testable without an embedding model.

## Next evolution

1. Extract text and page references from PDF/Form 16 uploads.
2. Replace keyword scoring with Spring AI embeddings + pgvector.
3. Persist documents, chunks, observations, and evidence (Mongo/Postgres).
4. Let Tax reconciliation consume structured extracted fields, not only source excerpts.

## Verification

```powershell
cd C:\Users\alapu\Workspace\ai-projects\openlifeops
.\mvnw.cmd verify -pl openlifeops-api -am
```

This runs four in-memory API tests plus two Failsafe MCP integration tests against the standalone Spring AI Streamable HTTP Tax MCP server.
