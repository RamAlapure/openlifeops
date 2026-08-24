package com.openlifeops.core.event;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
