package com.bk.modelcreator;

public interface EventHandler<T extends Event> {
    void handle(T event);
}

