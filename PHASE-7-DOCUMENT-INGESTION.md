# OpenLifeOps Phase 7 - Document Ingestion and Normalisation

## Status: complete (in-memory)

Phase 7 turns a local document upload into extracted, indexed text without changing the deterministic Tax workflow.

```text
multipart upload
  -> validate size, filename, type, and PDF header
  -> retain source bytes in process memory
  -> extract PDF text or decode text/plain
  -> KnowledgeService index
  -> existing Tax reconciliation and cited evidence
```

## Supported input

- `application/pdf` with embedded, extractable text, using Apache PDFBox 3.0.8.
- `text/plain` files, decoded as UTF-8.
- Maximum upload size: 10 MB.

The indexed document uses `text/plain` because that is the representation downstream retrieval and reconciliation consume. Source metadata remains available as document attributes:

- `ingestionMethod = multipart-upload`
- `sourceContentType`
- `sourceBytes`
- `extractedCharacters`
- `extractionStatus = SUCCESS`

Original bytes live in `InMemoryUploadedDocumentStore`; they disappear on application restart and are not exposed by an API endpoint.

## Errors and safety

- Empty or oversized uploads return `400 Bad Request`.
- Unsupported types and invalid PDF headers return `415 Unsupported Media Type`.
- Corrupt PDFs or PDFs with no extractable text return `422 Unprocessable Content`.
- Password-protected and scanned/image-only PDFs are deliberately unsupported. OCR is deferred.

No uploaded document is sent to an external system. The high-risk Tax action remains an approval-gated handoff to the internal OpenLifeOps Tax Review Queue.

## Verified path

The API integration tests create a non-sensitive in-memory Form 16-like PDF, upload it, verify the original bytes are retained in memory, retrieve its extracted text, and verify that the document appears in the resulting Tax report citations.

## Deliberately deferred

- OCR and image/document layout extraction
- Durable object storage and persistence
- PDF malware scanning / antivirus integration
- Spring AI reasoning
