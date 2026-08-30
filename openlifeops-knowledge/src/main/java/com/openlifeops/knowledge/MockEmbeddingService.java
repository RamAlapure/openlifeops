package com.openlifeops.knowledge;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Deterministic mock embedding service for testing and fallback.
 * Generates consistent embeddings based on text hash, avoiding external API calls.
 * Useful for development and testing without requiring AI service credentials.
 */
public class MockEmbeddingService implements EmbeddingService {

    private static final int DEFAULT_DIMENSION = 1536; // Match OpenAI text-embedding-ada-002
    private final int dimension;

    public MockEmbeddingService() {
        this(DEFAULT_DIMENSION);
    }

    public MockEmbeddingService(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public float[] embed(String text) {
        float[] embedding = new float[dimension];
        int hash = text.hashCode();
        
        // Generate deterministic but distributed values from hash
        for (int i = 0; i < dimension; i++) {
            // Use different bits of the hash for each dimension
            int combinedHash = hash ^ (i * 31);
            double normalized = Math.abs(combinedHash % 1000) / 1000.0;
            embedding[i] = (float) normalized;
        }
        
        // Normalize the vector
        return normalize(embedding);
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        return texts.stream()
                .map(this::embed)
                .collect(Collectors.toList());
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    private float[] normalize(float[] vector) {
        double norm = 0.0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        
        if (norm == 0.0) {
            return vector;
        }
        
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / (float) norm;
        }
        return normalized;
    }
}