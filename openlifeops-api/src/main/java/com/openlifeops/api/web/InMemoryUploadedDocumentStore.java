package com.openlifeops.api.web;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deliberately ephemeral storage for uploaded source bytes. Knowledge indexing stores only extracted text.
 */
@Component
public final class InMemoryUploadedDocumentStore {

    private final Map<String, UploadedDocument> documents = new ConcurrentHashMap<>();

    public void put(String documentId, byte[] bytes, String sourceContentType, int extractedCharacters) {
        documents.put(documentId, new UploadedDocument(
                Arrays.copyOf(bytes, bytes.length), sourceContentType, extractedCharacters, Instant.now()));
    }

    public Optional<UploadedDocument> find(String documentId) {
        return Optional.ofNullable(documents.get(documentId));
    }

    public record UploadedDocument(byte[] bytes, String sourceContentType, int extractedCharacters, Instant uploadedAt) {
        public UploadedDocument {
            bytes = Arrays.copyOf(bytes, bytes.length);
        }

        @Override
        public byte[] bytes() {
            return Arrays.copyOf(bytes, bytes.length);
        }
    }
}
