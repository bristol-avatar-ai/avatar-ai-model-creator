package com.bk.modelcreator;

import java.util.Properties;

public class PropertiesManager {
    private Properties properties;
    private String configPath;
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }
    public String getProperty(String key) {
        return properties.getProperty(key, "");
    }
    public String getConfigPath()
    {
        return configPath;
    }

    public void setConfigPath(String configPath)
    {
        this.configPath = configPath;
    }
}
