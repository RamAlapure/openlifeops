package com.openlifeops.knowledge;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenAI-based embedding service using Spring AI.
 * Requires OpenAI API key configuration.
 * This class uses reflection to handle optional Spring AI dependency.
 */
public class OpenAIEmbeddingService implements EmbeddingService {

    private final Object embeddingModel; // Spring AI EmbeddingModel (loaded via reflection)
    private final int dimension;

    public OpenAIEmbeddingService(Object embeddingModel, int dimension) {
        this.embeddingModel = embeddingModel;
        this.dimension = dimension;
    }

    @Override
    public float[] embed(String text) {
        try {
            // Use reflection to call Spring AI EmbeddingModel
            Class<?> embeddingRequestClass = Class.forName("org.springframework.ai.embedding.EmbeddingRequest");
            Class<?> embeddingResponseClass = Class.forName("org.springframework.ai.embedding.EmbeddingResponse");
            
            Object request = embeddingRequestClass.getConstructor(List.class, Object.class)
                    .newInstance(List.of(text), null);
            Object response = embeddingModel.getClass()
                    .getMethod("call", embeddingRequestClass)
                    .invoke(embeddingModel, request);
            
            List<?> results = (List<?>) embeddingResponseClass.getMethod("getResults")
                    .invoke(response);
            Object result = results.get(0);
            List<Double> embedding = (List<Double>) result.getClass()
                    .getMethod("getOutput")
                    .invoke(result);
            
            return toFloatArray(embedding);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate embedding using Spring AI", e);
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        try {
            Class<?> embeddingRequestClass = Class.forName("org.springframework.ai.embedding.EmbeddingRequest");
            Class<?> embeddingResponseClass = Class.forName("org.springframework.ai.embedding.EmbeddingResponse");
            
            Object request = embeddingRequestClass.getConstructor(List.class, Object.class)
                    .newInstance(texts, null);
            Object response = embeddingModel.getClass()
                    .getMethod("call", embeddingRequestClass)
                    .invoke(embeddingModel, request);
            
            List<?> results = (List<?>) embeddingResponseClass.getMethod("getResults")
                    .invoke(response);
            
            return results.stream()
                    .map(result -> {
                        try {
                            List<Double> embedding = (List<Double>) result.getClass()
                                    .getMethod("getOutput")
                                    .invoke(result);
                            return toFloatArray(embedding);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to extract embedding", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate batch embeddings using Spring AI", e);
        }
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    private float[] toFloatArray(List<Double> embedding) {
        float[] result = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            result[i] = embedding.get(i).floatValue();
        }
        return result;
    }
}