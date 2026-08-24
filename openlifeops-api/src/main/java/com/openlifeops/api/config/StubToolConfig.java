package com.openlifeops.api.config;

import com.openlifeops.mcp.StubToolRegistry;
import com.openlifeops.mcp.ToolRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("in-memory")
public class StubToolConfig {

    @Bean
    ToolRegistry baseToolRegistry() {
        return new StubToolRegistry();
    }
}
