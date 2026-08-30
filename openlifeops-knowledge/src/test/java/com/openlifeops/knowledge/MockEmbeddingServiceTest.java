package com.openlifeops.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockEmbeddingServiceTest {

    @Test
    void testEmbedGeneratesConsistentVectors() {
        MockEmbeddingService service = new MockEmbeddingService();
        
        String text1 = "test document";
        String text2 = "test document";
        String text3 = "different document";
        
        float[] embedding1 = service.embed(text1);
        float[] embedding2 = service.embed(text2);
        float[] embedding3 = service.embed(text3);
        
        // Same text should generate same embedding
        for (int i = 0; i < embedding1.length; i++) {
            assertEquals(embedding1[i], embedding2[i], 0.001f);
        }
        
        // Different text should generate different embedding
        assertFalse(java.util.Arrays.equals(embedding1, embedding3));
        
        // Check dimension
        assertEquals(1536, embedding1.length);
    }

    @Test
    void testEmbedBatch() {
        MockEmbeddingService service = new MockEmbeddingService();
        
        java.util.List<String> texts = java.util.List.of("doc1", "doc2", "doc3");
        java.util.List<float[]> embeddings = service.embedBatch(texts);
        
        assertEquals(3, embeddings.size());
        assertEquals(1536, embeddings.get(0).length);
        
        // Verify batch produces same results as individual calls
        float[] individual = service.embed("doc1");
        for (int i = 0; i < individual.length; i++) {
            assertEquals(individual[i], embeddings.get(0)[i], 0.001f);
        }
    }

    @Test
    void testCosineSimilarity() {
        MockEmbeddingService service = new MockEmbeddingService();
        
        float[] vec1 = service.embed("similar content");
        float[] vec2 = service.embed("similar content");
        float[] vec3 = service.embed("completely different text");
        
        // Identical vectors should have similarity 1.0
        assertEquals(1.0, service.cosineSimilarity(vec1, vec2), 0.001f);
        
        // Different vectors should have similarity < 1.0
        double similarity = service.cosineSimilarity(vec1, vec3);
        assertTrue(similarity >= 0.0 && similarity < 1.0);
    }

    @Test
    void testCustomDimension() {
        MockEmbeddingService service = new MockEmbeddingService(512);
        
        float[] embedding = service.embed("test");
        assertEquals(512, embedding.length);
        assertEquals(512, service.getDimension());
    }
}