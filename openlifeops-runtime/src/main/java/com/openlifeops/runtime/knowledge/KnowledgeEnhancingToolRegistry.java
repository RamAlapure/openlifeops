package com.openlifeops.runtime.knowledge;

import com.openlifeops.core.domain.Action;
import com.openlifeops.knowledge.KnowledgeDocumentJson;
import com.openlifeops.knowledge.KnowledgeService;
import com.openlifeops.core.knowledge.KnowledgeHit;
import com.openlifeops.mcp.ToolInvocationResult;
import com.openlifeops.mcp.ToolRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KnowledgeEnhancingToolRegistry implements ToolRegistry {

    private final ToolRegistry delegate;
    private final KnowledgeService knowledgeService;

    public KnowledgeEnhancingToolRegistry(ToolRegistry delegate, KnowledgeService knowledgeService) {
        this.delegate = delegate;
        this.knowledgeService = knowledgeService;
    }

    @Override
    public ToolInvocationResult invoke(String packId, String objective, Action action) {
        if ("tax.read_document".equals(action.getToolId())) {
            List<KnowledgeHit> hits = knowledgeService.retrieve(packId, buildQuery(objective, action), 1);
            if (!hits.isEmpty()) {
                KnowledgeHit hit = hits.getFirst();
                Map<String, String> metadata = new HashMap<>(KnowledgeDocumentJson.citationMetadata(hit));
                metadata.put("citation", "true");
                return new ToolInvocationResult(
                        KnowledgeDocumentJson.fromHit(hit),
                        "knowledge:" + hit.getDocument().getId(),
                        0L,
                        metadata);
            }
        }
        return delegate.invoke(packId, objective, action);
    }

    private static String buildQuery(String objective, Action action) {
        StringBuilder builder = new StringBuilder();
        if (objective != null && !objective.isBlank()) {
            builder.append(objective.strip());
        }
        if (action.getTarget() != null && !action.getTarget().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(action.getTarget().strip());
        }
        return builder.toString();
    }
}
