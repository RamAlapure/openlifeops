package com.openlifeops.api.web;

import com.openlifeops.core.knowledge.IngestDocumentCommand;
import com.openlifeops.core.knowledge.KnowledgeDocument;
import com.openlifeops.core.knowledge.KnowledgeHit;
import com.openlifeops.knowledge.KnowledgeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Text and PDF ingestion endpoint. OCR is intentionally deferred.
 */
@RestController
@Validated
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final KnowledgeService knowledgeService;
    private final DocumentUploadService documentUploadService;

    public DocumentController(KnowledgeService knowledgeService, DocumentUploadService documentUploadService) {
        this.knowledgeService = knowledgeService;
        this.documentUploadService = documentUploadService;
    }

    @PostMapping
    public DocumentResponse ingest(@Valid @RequestBody IngestDocumentRequest request) {
        KnowledgeDocument document = knowledgeService.ingest(new IngestDocumentCommand(
                request.pack(),
                request.documentType(),
                request.fileName(),
                request.contentType(),
                request.content(),
                request.attributes()));
        return DocumentResponse.from(document);
    }

    @PostMapping(path = "/upload", consumes = "multipart/form-data")
    public DocumentResponse upload(
            @RequestParam @NotBlank String pack,
            @RequestParam @NotBlank String documentType,
            @RequestParam("file") MultipartFile file) {
        return DocumentResponse.from(documentUploadService.ingest(pack, documentType, file));
    }

    @GetMapping
    public List<DocumentResponse> list(@RequestParam @NotBlank String pack) {
        return knowledgeService.listByPack(pack).stream().map(DocumentResponse::from).toList();
    }

    @GetMapping("/search")
    public List<KnowledgeHitResponse> search(
            @RequestParam @NotBlank String pack,
            @RequestParam @NotBlank String query,
            @RequestParam(defaultValue = "5") int limit) {
        return knowledgeService.retrieve(pack, query, Math.max(1, Math.min(limit, 20))).stream()
                .map(KnowledgeHitResponse::from)
                .toList();
    }

    @GetMapping("/{documentId}")
    public DocumentResponse get(@PathVariable String documentId) {
        return knowledgeService.findById(documentId)
                .map(DocumentResponse::from)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    public record IngestDocumentRequest(
            @NotBlank String pack,
            @NotBlank String documentType,
            @NotBlank String fileName,
            String contentType,
            @NotBlank String content,
            Map<String, String> attributes) {
    }

    public record DocumentResponse(
            String id,
            String pack,
            String documentType,
            String fileName,
            String contentType,
            Map<String, String> attributes) {
        static DocumentResponse from(KnowledgeDocument document) {
            return new DocumentResponse(
                    document.getId(),
                    document.getPackId(),
                    document.getDocumentType(),
                    document.getFileName(),
                    document.getContentType(),
                    document.getAttributes());
        }
    }

    public record KnowledgeHitResponse(
            String documentId,
            String fileName,
            String chunkId,
            String excerpt,
            double score) {
        static KnowledgeHitResponse from(KnowledgeHit hit) {
            return new KnowledgeHitResponse(
                    hit.getDocument().getId(),
                    hit.getDocument().getFileName(),
                    hit.getChunk().getId(),
                    hit.getChunk().getExcerpt(),
                    hit.getScore());
        }
    }

    public static final class DocumentNotFoundException extends RuntimeException {
        DocumentNotFoundException(String documentId) {
            super("Document not found: " + documentId);
        }
    }
}
