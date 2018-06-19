package org.aion.gui.events;

import com.google.common.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class EventBusRegistry {

    private static final EventBusRegistry INSTANCE = new EventBusRegistry();

    private final Map<String, EventBus> busMap = new HashMap<>();

    public static EventBus getBus(final String identifier) {
        return INSTANCE.getBusById(identifier);
    }

    private EventBusRegistry() {}

    private EventBus getBusById(final String identifier) {
        return busMap.computeIfAbsent(identifier, EventBus::new);
    }
}
