package com.bk.modelcreator;

public class InvalidFolderSelectedEvent implements Event{
    @Override
    public String getMessage() {
        return "Invalid folder selected";
    }
}
