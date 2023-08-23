package com.bk.modelcreator;

import com.google.common.eventbus.EventBus;

/**
 * Global Event Bus class created for messages to be communicated between different
 * classes.
 */
public class GlobalEventBus {
    private static final EventBus eventBus = new EventBus();

    public static EventBus getInstance() {
        return eventBus;
    }
}