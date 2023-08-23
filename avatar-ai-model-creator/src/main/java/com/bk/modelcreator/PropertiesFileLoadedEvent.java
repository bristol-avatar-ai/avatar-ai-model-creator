package com.bk.modelcreator;

import java.util.Properties;

public class PropertiesFileLoadedEvent {
    private boolean isLoaded;
    private String message;
    private Properties properties;

    public PropertiesFileLoadedEvent(boolean isLoaded, String message, Properties properties) {
        this.isLoaded = isLoaded;
        this.message = message;
        this.properties = properties;
    }

    public boolean isLoaded()
    {
        return isLoaded;
    }

    public String getMessage()
    {
        return message;
    }

    public Properties getProperties()
    {
        return properties;
    }
}

