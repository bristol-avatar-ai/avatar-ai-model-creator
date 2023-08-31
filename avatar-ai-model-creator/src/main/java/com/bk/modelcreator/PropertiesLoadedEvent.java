package com.bk.modelcreator;

public class PropertiesLoadedEvent implements Event{

    @Override
    public String getMessage() {
        return "Loaded properties";
    }

}
