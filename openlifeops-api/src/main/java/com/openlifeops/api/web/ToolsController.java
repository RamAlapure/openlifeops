package com.openlifeops.api.web;

import com.openlifeops.api.mcp.ToolDiscoveryService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tools")
@Profile("mcp")
public class ToolsController {

    private final ToolDiscoveryService toolDiscoveryService;

    public ToolsController(ToolDiscoveryService toolDiscoveryService) {
        this.toolDiscoveryService = toolDiscoveryService;
    }

    @GetMapping
    public List<ToolResponse> listTools() {
        return toolDiscoveryService.listTools().stream()
                .map(tool -> new ToolResponse(tool.serverName(), tool.toolName(), tool.description()))
                .toList();
    }

    public record ToolResponse(String serverName, String toolName, String description) {
    }
}
