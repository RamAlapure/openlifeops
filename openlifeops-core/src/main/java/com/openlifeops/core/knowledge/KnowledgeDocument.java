package com.openlifeops.core.knowledge;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class KnowledgeDocument {

    private final String id;
    private final String packId;
    private final String documentType;
    private final String fileName;
    private final String contentType;
    private final Instant ingestedAt;
    private final Map<String, String> attributes;

    public KnowledgeDocument(
            String id,
            String packId,
            String documentType,
            String fileName,
            String contentType,
            Instant ingestedAt,
            Map<String, String> attributes) {
        this.id = Objects.requireNonNull(id, "id");
        this.packId = Objects.requireNonNull(packId, "packId");
        this.documentType = Objects.requireNonNull(documentType, "documentType");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.contentType = Objects.requireNonNull(contentType, "contentType");
        this.ingestedAt = Objects.requireNonNull(ingestedAt, "ingestedAt");
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public String getId() {
        return id;
    }

    public String getPackId() {
        return packId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
