package com.bk.modelcreator;

import java.util.ArrayList;
import java.util.List;

public class Logger {
    private static Logger instance;
    private final List<LogObserver> observers;
    private Logger() {
        this.observers = new ArrayList<>();
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void addObserver(LogObserver observer) {
        this.observers.add(observer);
    }

    public void log(String message) {
        notifyObservers(message);
    }

    private void notifyObservers(String message) {
        for (LogObserver observer : this.observers) {
            observer.update(message);
        }
    }
}