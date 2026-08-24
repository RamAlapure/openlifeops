package com.openlifeops.runtime.pack;

import com.openlifeops.core.descriptor.WorkflowDefinition;
import com.openlifeops.core.pack.OpenLifeOpsPack;
import com.openlifeops.core.pack.PackNotFoundException;
import com.openlifeops.core.pack.WorkflowNotFoundException;
import com.openlifeops.core.workflow.ResolvedWorkflow;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PackRegistry {

    private final Map<String, OpenLifeOpsPack> packs = new ConcurrentHashMap<>();

    public void register(OpenLifeOpsPack pack) {
        packs.put(pack.id(), pack);
    }

    public OpenLifeOpsPack require(String packId) {
        OpenLifeOpsPack pack = packs.get(packId);
        if (pack == null) {
            throw new PackNotFoundException(packId);
        }
        return pack;
    }

    public Collection<OpenLifeOpsPack> all() {
        return packs.values();
    }

    public ResolvedWorkflow getWorkflow(String packId, String workflowId) {
        OpenLifeOpsPack pack = require(packId);
        WorkflowDefinition definition = pack.workflows().stream()
                .filter(workflow -> workflow.id().equals(workflowId))
                .findFirst()
                .orElseThrow(() -> new WorkflowNotFoundException(packId, workflowId));
        return new ResolvedWorkflow(
                definition,
                pack.workflowVersion(workflowId),
                pack.workflowSteps(workflowId));
    }

    public ResolvedWorkflow getDefaultWorkflow(String packId) {
        OpenLifeOpsPack pack = require(packId);
        if (pack.workflows().isEmpty()) {
            throw new WorkflowNotFoundException(packId, "default");
        }
        WorkflowDefinition definition = pack.workflows().getFirst();
        return getWorkflow(packId, definition.id());
    }
}
