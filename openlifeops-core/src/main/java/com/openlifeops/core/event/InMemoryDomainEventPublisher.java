package com.openlifeops.core.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class InMemoryDomainEventPublisher implements DomainEventPublisher {

    private final List<DomainEvent> events = new ArrayList<>();
    private final List<Consumer<DomainEvent>> listeners = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        events.add(event);
        for (Consumer<DomainEvent> listener : listeners) {
            listener.accept(event);
        }
    }

    public List<DomainEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addListener(Consumer<DomainEvent> listener) {
        listeners.add(listener);
    }
}
