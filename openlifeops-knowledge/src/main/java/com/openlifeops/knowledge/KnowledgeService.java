package com.openlifeops.knowledge;

import com.openlifeops.core.knowledge.IngestDocumentCommand;
import com.openlifeops.core.knowledge.KnowledgeDocument;
import com.openlifeops.core.knowledge.KnowledgeHit;

import java.util.List;
import java.util.Optional;

public interface KnowledgeService {

    KnowledgeDocument ingest(IngestDocumentCommand command);

    List<KnowledgeHit> retrieve(String packId, String query, int limit);

    /** Returns every indexed chunk for deterministic pack-level workflows. */
    List<KnowledgeHit> allChunks(String packId);

    List<KnowledgeDocument> listByPack(String packId);

    Optional<KnowledgeDocument> findById(String documentId);
}
