package com.openlifeops.knowledge;

import java.util.List;

/**
 * Service for generating vector embeddings from text.
 * Implementations can use different embedding models (OpenAI, local models, etc.)
 * or provide deterministic fallbacks for testing.
 */
public interface EmbeddingService {

    /**
     * Generate embedding vector for a single text.
     *
     * @param text the text to embed
     * @return embedding vector (float array)
     */
    float[] embed(String text);

    /**
     * Generate embedding vectors for multiple texts in batch.
     * More efficient than calling embed() multiple times.
     *
     * @param texts list of texts to embed
     * @return list of embedding vectors
     */
    List<float[]> embedBatch(List<String> texts);

    /**
     * Calculate cosine similarity between two embedding vectors.
     *
     * @param vec1 first embedding vector
     * @param vec2 second embedding vector
     * @return similarity score between 0.0 and 1.0
     */
    default double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Get the dimension of the embedding vectors produced by this service.
     *
     * @return embedding dimension
     */
    int getDimension();
}