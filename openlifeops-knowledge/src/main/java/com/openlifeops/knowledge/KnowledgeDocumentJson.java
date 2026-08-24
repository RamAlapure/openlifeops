package com.openlifeops.knowledge;

import com.openlifeops.core.knowledge.KnowledgeDocument;
import com.openlifeops.core.knowledge.KnowledgeHit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class KnowledgeDocumentJson {

    private KnowledgeDocumentJson() {
    }

    public static String fromHit(KnowledgeHit hit) {
        KnowledgeDocument document = hit.getDocument();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("source", document.getFileName());
        payload.put("documentId", document.getId());
        payload.put("chunkId", hit.getChunk().getId());
        payload.put("documentType", document.getDocumentType());
        payload.put("excerpt", hit.getChunk().getExcerpt());
        payload.put("content", hit.getChunk().getContent());
        document.getAttributes().forEach(payload::putIfAbsent);
        return toJson(payload);
    }

    public static Map<String, String> citationMetadata(KnowledgeHit hit) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("documentId", hit.getDocument().getId());
        metadata.put("chunkId", hit.getChunk().getId());
        metadata.put("excerpt", hit.getChunk().getExcerpt());
        metadata.put("documentType", hit.getDocument().getDocumentType());
        metadata.put("fileName", hit.getDocument().getFileName());
        return Map.copyOf(metadata);
    }

    private static String toJson(Map<String, Object> payload) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof Number number) {
                builder.append(number);
            } else {
                builder.append('"').append(escape(String.valueOf(value))).append('"');
            }
        }
        builder.append('}');
        return builder.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
