package com.openlifeops.knowledge;

import com.openlifeops.core.knowledge.DocumentChunk;
import com.openlifeops.core.knowledge.KnowledgeHit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

final class KeywordRetriever {

    private KeywordRetriever() {
    }

    static List<KnowledgeHit> rank(String query, List<IndexedChunk> chunks, int limit) {
        Set<String> terms = tokenize(query);
        if (terms.isEmpty()) {
            return List.of();
        }
        List<ScoredHit> scored = new ArrayList<>();
        for (IndexedChunk indexedChunk : chunks) {
            double score = score(terms, indexedChunk.chunk().getContent());
            if (score > 0) {
                scored.add(new ScoredHit(indexedChunk, score));
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

    private static double score(Set<String> terms, String content) {
        Set<String> contentTerms = tokenize(content);
        if (contentTerms.isEmpty()) {
            return 0;
        }
        long matches = terms.stream().filter(contentTerms::contains).count();
        return (double) matches / terms.size();
    }

    private static Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() > 2)
                .collect(Collectors.toSet());
    }

    record IndexedChunk(DocumentChunk chunk, com.openlifeops.core.knowledge.KnowledgeDocument document) {
    }

    private record ScoredHit(IndexedChunk indexedChunk, double score) {
    }
}
