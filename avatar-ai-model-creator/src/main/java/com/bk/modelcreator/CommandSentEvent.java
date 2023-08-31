package com.bk.modelcreator;

public class CommandSentEvent implements Event{
    @Override
    public String getMessage() {
        return "Processed command - feel free to close the application";
    }
}
