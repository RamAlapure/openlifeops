package com.openlifeops.knowledge;

import com.openlifeops.core.knowledge.DocumentChunk;
import com.openlifeops.core.knowledge.IngestDocumentCommand;
import com.openlifeops.core.knowledge.KnowledgeDocument;
import com.openlifeops.core.knowledge.KnowledgeHit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemoryKnowledgeService implements KnowledgeService {

    private final Map<String, KnowledgeDocument> documents = new ConcurrentHashMap<>();
    private final Map<String, List<DocumentChunk>> chunksByDocument = new ConcurrentHashMap<>();

    public InMemoryKnowledgeService() {
    }

    @Override
    public KnowledgeDocument ingest(IngestDocumentCommand command) {
        String documentId = UUID.randomUUID().toString();
        Instant ingestedAt = Instant.now();
        KnowledgeDocument document = new KnowledgeDocument(
                documentId,
                command.getPackId(),
                command.getDocumentType(),
                command.getFileName(),
                command.getContentType(),
                ingestedAt,
                command.getAttributes());
        documents.put(documentId, document);

        List<String> chunkTexts = TextChunker.chunk(command.getContent());
        List<DocumentChunk> chunks = new ArrayList<>();
        for (int index = 0; index < chunkTexts.size(); index++) {
            String text = chunkTexts.get(index);
            String excerpt = text.length() > 160 ? text.substring(0, 160) + "..." : text;
            chunks.add(new DocumentChunk(
                    UUID.randomUUID().toString(),
                    documentId,
                    command.getPackId(),
                    index,
                    text,
                    excerpt));
        }
        chunksByDocument.put(documentId, List.copyOf(chunks));
        return document;
    }

    @Override
    public List<KnowledgeHit> retrieve(String packId, String query, int limit) {
        List<KeywordRetriever.IndexedChunk> indexedChunks = documents.values().stream()
                .filter(document -> document.getPackId().equals(packId))
                .flatMap(document -> chunksByDocument.getOrDefault(document.getId(), List.of()).stream()
                        .map(chunk -> new KeywordRetriever.IndexedChunk(chunk, document)))
                .collect(Collectors.toList());
        return KeywordRetriever.rank(query, indexedChunks, limit);
    }

    @Override
    public List<KnowledgeHit> allChunks(String packId) {
        return chunksByDocument.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(chunk -> {
                            KnowledgeDocument doc = documents.get(entry.getKey());
                            return new KnowledgeHit(chunk, doc, 1.0);
                        }))
                .filter(hit -> hit.getDocument().getPackId().equals(packId))
                .sorted(java.util.Comparator
                        .comparing((KnowledgeHit hit) -> hit.getDocument().getFileName())
                        .thenComparingInt(hit -> hit.getChunk().getIndex()))
                .toList();
    }

    @Override
    public List<KnowledgeDocument> listByPack(String packId) {
        return documents.values().stream()
                .filter(document -> document.getPackId().equals(packId))
                .sorted((left, right) -> right.getIngestedAt().compareTo(left.getIngestedAt()))
                .toList();
    }

    @Override
    public Optional<KnowledgeDocument> findById(String documentId) {
        return Optional.ofNullable(documents.get(documentId));
    }
}
