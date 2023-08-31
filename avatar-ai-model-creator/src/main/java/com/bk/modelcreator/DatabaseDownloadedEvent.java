package com.bk.modelcreator;

public class DatabaseDownloadedEvent implements Event{
    private String dbPath;
    public DatabaseDownloadedEvent(String dbPath)
    {
        this.dbPath = dbPath;
    }

    public String getDbPath()
    {
        return dbPath;
    }
    @Override
    public String getMessage() {
        return "Database downloaded";
    }
}
