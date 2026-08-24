package com.openlifeops.core.knowledge;

import java.util.Map;
import java.util.Objects;

public final class IngestDocumentCommand {

    private final String packId;
    private final String documentType;
    private final String fileName;
    private final String contentType;
    private final String content;
    private final Map<String, String> attributes;

    public IngestDocumentCommand(
            String packId,
            String documentType,
            String fileName,
            String contentType,
            String content,
            Map<String, String> attributes) {
        this.packId = Objects.requireNonNull(packId, "packId");
        this.documentType = Objects.requireNonNull(documentType, "documentType");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.contentType = contentType == null ? "text/plain" : contentType;
        this.content = Objects.requireNonNull(content, "content");
        this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public String getPackId() {
        return packId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContent() {
        return content;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
