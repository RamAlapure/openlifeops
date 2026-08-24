package com.openlifeops.api.config;

import com.openlifeops.mcp.ToolRegistry;
import com.openlifeops.runtime.mcp.McpToolRegistry;
import com.openlifeops.runtime.mcp.PackToolResolver;
import com.openlifeops.runtime.pack.PackRegistry;
import com.openlifeops.api.mcp.SpringMcpToolInvoker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("mcp")
@EnableConfigurationProperties(OpenLifeOpsMcpProperties.class)
public class McpToolConfig {

    @Bean
    PackToolResolver packToolResolver(PackRegistry packRegistry) {
        return new PackToolResolver(packRegistry);
    }

    @Bean
    ToolRegistry baseToolRegistry(PackToolResolver packToolResolver, SpringMcpToolInvoker springMcpToolInvoker) {
        return new McpToolRegistry(packToolResolver, springMcpToolInvoker);
    }
}
