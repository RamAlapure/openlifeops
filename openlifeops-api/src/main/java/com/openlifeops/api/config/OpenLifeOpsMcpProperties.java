package com.openlifeops.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "openlifeops.mcp")
public class OpenLifeOpsMcpProperties {

    private Map<String, String> serverConnections = new LinkedHashMap<>(Map.of("tax-tools", "tax-tools"));

    public Map<String, String> getServerConnections() {
        return serverConnections;
    }

    public void setServerConnections(Map<String, String> serverConnections) {
        this.serverConnections = serverConnections;
    }
}
