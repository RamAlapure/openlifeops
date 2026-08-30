package com.openlifeops.core.knowledge;

import java.util.Objects;

public final class DocumentChunk {

    private final String id;
    private final String documentId;
    private final String packId;
    private final int index;
    private final String content;
    private final String excerpt;
    private final float[] embedding; // Optional vector embedding for semantic search

    public DocumentChunk(
            String id,
            String documentId,
            String packId,
            int index,
            String content,
            String excerpt) {
        this(id, documentId, packId, index, content, excerpt, null);
    }

    public DocumentChunk(
            String id,
            String documentId,
            String packId,
            int index,
            String content,
            String excerpt,
            float[] embedding) {
        this.id = Objects.requireNonNull(id, "id");
        this.documentId = Objects.requireNonNull(documentId, "documentId");
        this.packId = Objects.requireNonNull(packId, "packId");
        this.index = index;
        this.content = Objects.requireNonNull(content, "content");
        this.excerpt = Objects.requireNonNull(excerpt, "excerpt");
        this.embedding = embedding; // Can be null for backward compatibility
    }

    public String getId() {
        return id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getPackId() {
        return packId;
    }

    public int getIndex() {
        return index;
    }

    public String getContent() {
        return content;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public float[] getEmbedding() {
        return embedding;
    }
}
