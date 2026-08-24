package com.openlifeops.core.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class Action {

    private final String id;
    private final ActionType type;
    private final String target;
    private final String toolId;
    private final RiskLevel risk;
    private final String description;
    private final Map<String, String> parameters;

    public Action(
            String id,
            ActionType type,
            String target,
            String toolId,
            RiskLevel risk,
            String description,
            Map<String, String> parameters) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.target = Objects.requireNonNull(target, "target");
        this.toolId = Objects.requireNonNull(toolId, "toolId");
        this.risk = Objects.requireNonNull(risk, "risk");
        this.description = Objects.requireNonNull(description, "description");
        this.parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    public String getId() {
        return id;
    }

    public ActionType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public String getToolId() {
        return toolId;
    }

    public RiskLevel getRisk() {
        return risk;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
}
