package com.bk.modelcreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private final Map<Class<? extends Event>, List<EventHandler<? extends Event>>> subscribers = new HashMap<>();
    private Logger logger = Logger.getInstance();
    public <T extends Event> void subscribe(Class<? extends T> eventType, EventHandler<T> handler)
    {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    public <T extends Event> void publish(T event) {
        logger.log("[" + event.getTimeStamp() + "]" + " " + event.getMessage());
        Class<? extends Event> eventType = event.getClass();
        List<EventHandler<? extends Event>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            for (EventHandler handler : handlers) {
                handler.handle(event);
            }
        }
    }
}
