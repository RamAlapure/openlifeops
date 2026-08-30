# OpenLifeOps Phase 4 — Knowledge and Cited Evidence

## Status: enhanced with vector embeddings (in-memory baseline)

Phase 4 grounds the Tax pack in ingested source material instead of only canned tool responses, now with semantic search capabilities.

```text
POST /api/v1/documents
  → InMemoryKnowledgeService
  → paragraph-aware chunks + vector embeddings (optional)
  → hybrid retrieval (vector + keyword fallback)
  → tax.read_document
  → Observation + immutable Evidence with citation metadata
```

## What is implemented

- Pack-scoped text document ingestion and listing: `POST` / `GET /api/v1/documents`
- Retrieval endpoint: `GET /api/v1/documents/search?pack=tax&query=...`
- Immutable knowledge document/chunk/hit domain model in `openlifeops-core`
- In-memory chunk storage with **vector embedding support** in `openlifeops-knowledge`
- **VectorRetriever**: Semantic search using cosine similarity between embeddings
- **EmbeddingService interface**: Pluggable embedding models (OpenAI, mock for testing)
- **MockEmbeddingService**: Deterministic fallback for development without AI credentials
- **OpenAIEmbeddingService**: Production-ready OpenAI embeddings via Spring AI (optional)
- **Hybrid retrieval**: Vector similarity with keyword fallback when embeddings unavailable
- `KnowledgeEnhancingToolRegistry`: `tax.read_document` uses the best Tax-document hit; other tools retain their configured stub/MCP path.
- Cited evidence metadata: `documentId`, `chunkId`, `excerpt`, document type, and filename.
- Regression tests proving ingest → retrieve → Tax task → cited evidence.

## New Capabilities

### Vector Embeddings
- `DocumentChunk` now supports optional embedding vectors for semantic search
- `EmbeddingService` interface provides pluggable embedding implementations
- `InMemoryKnowledgeService` automatically generates embeddings during document ingestion when an embedding service is configured
- Batch embedding generation for efficient processing of multiple chunks

### Semantic Search
- `VectorRetriever` ranks chunks by cosine similarity between query and chunk embeddings
- `rankHybrid()` method provides intelligent fallback: vector search when available, keyword matching when not
- Improved search quality for conceptually similar content beyond exact keyword matches

### Configuration
- Default: `MockEmbeddingService` (deterministic, no external dependencies)
- Optional: `OpenAIEmbeddingService` when `spring.ai.openai.api-key` is configured
- Configurable embedding dimension (default: 1536 for OpenAI text-embedding-ada-002)
- Spring Boot auto-configuration for seamless OpenAI integration

## Current limitation

This is an **enhanced prototype with vector capabilities**, not a production RAG implementation:

- No multipart upload, PDF parsing, OCR, or pgvector yet (PDF upload added in Phase 7).
- Data is process-local under the `in-memory` persistence profile.
- Vector storage is in-memory; no persistent vector database.
- No distributed search or large-scale document management.

## Next evolution

1. Extract text and page references from PDF/Form 16 uploads (completed in Phase 7).
2. Add pgvector or dedicated vector database for persistent vector storage.
3. Persist documents, chunks, observations, and evidence (Mongo/Postgres).
4. Let Tax reconciliation consume structured extracted fields, not only source excerpts.

## Configuration

### Default (Mock Embeddings)
No configuration needed. The system uses deterministic mock embeddings for development and testing.

### OpenAI Embeddings (Optional)
Add to `application.yml`:
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      embedding:
        options:
          model: text-embedding-ada-002

openlifeops:
  knowledge:
    embedding:
      dimension: 1536
```

When `OPENAI_API_KEY` is configured, the system automatically uses OpenAI embeddings for semantic search.

## Verification

```powershell
cd C:\Users\alapu\Workspace\ai-projects\openlifeops
.\mvnw.cmd verify -pl openlifeops-api -am
```

This runs:
- Four in-memory API tests (unchanged)
- Vector embedding service tests (new)
- Two Failsafe MCP integration tests against the standalone Spring AI Streamable HTTP Tax MCP server (unchanged)

### Testing Vector Retrieval
```powershell
.\mvnw.cmd test -pl openlifeops-knowledge
```

Tests the embedding service and vector retrieval functionality.
