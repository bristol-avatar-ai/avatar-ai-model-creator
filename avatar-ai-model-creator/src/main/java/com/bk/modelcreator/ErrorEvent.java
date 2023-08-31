package com.bk.modelcreator;

public class ErrorEvent implements Event{
    private String eventDescription;
    public ErrorEvent(String eventDescription)
    {
        this.eventDescription = eventDescription;
    }
    @Override
    public String getMessage() {
        return eventDescription;
    }
}
