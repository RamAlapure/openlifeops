package com.openlifeops.core.knowledge;

import java.util.Objects;

public final class KnowledgeHit {

    private final DocumentChunk chunk;
    private final KnowledgeDocument document;
    private final double score;

    public KnowledgeHit(DocumentChunk chunk, KnowledgeDocument document, double score) {
        this.chunk = Objects.requireNonNull(chunk, "chunk");
        this.document = Objects.requireNonNull(document, "document");
        this.score = score;
    }

    public DocumentChunk getChunk() {
        return chunk;
    }

    public KnowledgeDocument getDocument() {
        return document;
    }

    public double getScore() {
        return score;
    }
}
