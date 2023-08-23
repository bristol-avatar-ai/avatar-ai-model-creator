package com.bk.modelcreator;

public class MainWindowProcessingEvent {
    private boolean isBusy;
    public MainWindowProcessingEvent(boolean isBusy) {
        this.isBusy = isBusy;
    }
    public boolean isBusy() {
        return isBusy;
    }
}
