package com.openlifeops.packs.tax;

import com.openlifeops.core.knowledge.KnowledgeHit;

import java.util.Objects;

public record TaxDocumentFacts(
        String documentId,
        String chunkId,
        String fileName,
        String excerpt,
        String pan,
        String financialYear,
        String employer,
        Long income,
        Long tds) {

    public TaxDocumentFacts {
        Objects.requireNonNull(documentId, "documentId");
        Objects.requireNonNull(chunkId, "chunkId");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(excerpt, "excerpt");
    }

    static TaxDocumentFacts from(KnowledgeHit hit, String pan, String financialYear, String employer, Long income, Long tds) {
        return new TaxDocumentFacts(
                hit.getDocument().getId(),
                hit.getChunk().getId(),
                hit.getDocument().getFileName(),
                hit.getChunk().getExcerpt(),
                pan,
                financialYear,
                employer,
                income,
                tds);
    }
}
