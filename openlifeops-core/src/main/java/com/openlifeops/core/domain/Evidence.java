package com.openlifeops.core.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable evidence record. Agents append new evidence; they never mutate existing records.
 */
public final class Evidence {

    private final String id;
    private final String taskId;
    private final String executionId;
    private final String source;
    private final String sourceReference;
    private final String claim;
    private final String extractedValue;
    private final Instant timestamp;
    private final String provenance;
    private final Map<String, String> metadata;

    public Evidence(
            String id,
            String taskId,
            String executionId,
            String source,
            String sourceReference,
            String claim,
            String extractedValue,
            Instant timestamp,
            String provenance,
            Map<String, String> metadata) {
        this.id = Objects.requireNonNull(id, "id");
        this.taskId = Objects.requireNonNull(taskId, "taskId");
        this.executionId = Objects.requireNonNull(executionId, "executionId");
        this.source = Objects.requireNonNull(source, "source");
        this.sourceReference = Objects.requireNonNull(sourceReference, "sourceReference");
        this.claim = Objects.requireNonNull(claim, "claim");
        this.extractedValue = extractedValue;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.provenance = Objects.requireNonNull(provenance, "provenance");
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public String getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getSource() {
        return source;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public String getClaim() {
        return claim;
    }

    public String getExtractedValue() {
        return extractedValue;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getProvenance() {
        return provenance;
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
}
