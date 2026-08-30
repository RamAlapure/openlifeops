package com.openlifeops.knowledge;

import com.openlifeops.core.knowledge.DocumentChunk;
import com.openlifeops.core.knowledge.KnowledgeDocument;
import com.openlifeops.core.knowledge.KnowledgeHit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vector-based retriever using semantic similarity via embeddings.
 * Provides more intelligent search compared to keyword matching.
 */
final class VectorRetriever {

    private VectorRetriever() {
    }

    /**
     * Rank chunks by vector similarity to the query.
     *
     * @param queryEmbedding embedding of the search query
     * @param indexedChunks list of chunks with their embeddings
     * @param embeddingService service for calculating similarity
     * @param limit maximum number of results to return
     * @return ranked knowledge hits
     */
    static List<KnowledgeHit> rank(
            float[] queryEmbedding,
            List<IndexedChunk> indexedChunks,
            EmbeddingService embeddingService,
            int limit) {
        
        List<ScoredHit> scored = new ArrayList<>();
        for (IndexedChunk indexedChunk : indexedChunks) {
            if (indexedChunk.embedding() != null) {
                double similarity = embeddingService.cosineSimilarity(
                        queryEmbedding, 
                        indexedChunk.embedding());
                if (similarity > 0.0) {
                    scored.add(new ScoredHit(indexedChunk, similarity));
                }
            }
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredHit::score).reversed())
                .limit(limit)
                .map(scoredHit -> new KnowledgeHit(
                        scoredHit.indexedChunk().chunk(),
                        scoredHit.indexedChunk().document(),
                        scoredHit.score()))
                .toList();
    }

    /**
     * Rank chunks using hybrid approach: vector similarity with keyword fallback.
     * If no embeddings are available, falls back to keyword matching.
     */
    static List<KnowledgeHit> rankHybrid(
            String query,
            float[] queryEmbedding,
            List<IndexedChunk> indexedChunks,
            EmbeddingService embeddingService,
            int limit) {
        
        // Check if we have embeddings for any chunks
        boolean hasEmbeddings = indexedChunks.stream()
                .anyMatch(chunk -> chunk.embedding() != null);

        if (hasEmbeddings && queryEmbedding != null) {
            // Use vector similarity
            return rank(queryEmbedding, indexedChunks, embeddingService, limit);
        } else {
            // Fall back to keyword matching
            return KeywordRetriever.rank(query, 
                    indexedChunks.stream()
                            .map(ic -> new KeywordRetriever.IndexedChunk(ic.chunk(), ic.document()))
                            .collect(Collectors.toList()), 
                    limit);
        }
    }

    record IndexedChunk(
            DocumentChunk chunk, 
            KnowledgeDocument document, 
            float[] embedding) {
    }

    private record ScoredHit(IndexedChunk indexedChunk, double score) {
    }
}