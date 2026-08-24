package com.openlifeops.core.api;

import java.util.Objects;

public final class TaskRequest {

    private final String objective;
    private final String pack;

    public TaskRequest(String objective, String pack) {
        this.objective = Objects.requireNonNull(objective, "objective");
        this.pack = Objects.requireNonNull(pack, "pack");
    }

    public String getObjective() {
        return objective;
    }

    public String getPack() {
        return pack;
    }
}
