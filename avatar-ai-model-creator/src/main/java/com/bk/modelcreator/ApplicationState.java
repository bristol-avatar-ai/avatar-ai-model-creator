package com.bk.modelcreator;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;

public class ApplicationState {

    public enum State {
        INIT,
        PROPERTIES_SELECTED,
        PROCESSING

    }
    private final ObjectProperty<State> currentState = new SimpleObjectProperty<>(State.INIT);

    public ObjectProperty<State> currentStateProperty() {
        return currentState;
    }

    public State getCurrentState() {
        return currentState.get();
    }

    public void setCurrentState(State currentState) {
        this.currentState.set(currentState);
    }

    public void addStateChangeListener(ChangeListener<State> listener) {
        currentState.addListener(listener);
    }

}

