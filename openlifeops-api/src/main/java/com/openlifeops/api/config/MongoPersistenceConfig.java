package com.openlifeops.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("mongo")
@Import(InMemoryPersistenceConfig.class)
public class MongoPersistenceConfig {
    // Phase 2: Mongo adapters will replace in-memory beans under this profile.
    // For now, mongo profile delegates to in-memory until document mappers land.
}
