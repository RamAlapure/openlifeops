package com.openlifeops.core.pack;

public class WorkflowNotFoundException extends RuntimeException {

    public WorkflowNotFoundException(String packId, String workflowId) {
        super("Workflow not found: pack=" + packId + ", workflow=" + workflowId);
    }
}
