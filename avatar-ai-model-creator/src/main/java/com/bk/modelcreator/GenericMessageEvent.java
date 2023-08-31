package com.bk.modelcreator;

public class GenericMessageEvent implements Event{
    private String message;
    public GenericMessageEvent(String message)
    {
        this.message = message;
    }
    @Override
    public String getMessage() {
        return message;
    }
}
