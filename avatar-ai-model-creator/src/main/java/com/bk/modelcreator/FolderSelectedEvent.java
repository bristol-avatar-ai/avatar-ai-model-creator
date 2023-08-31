package com.bk.modelcreator;

public class FolderSelectedEvent implements Event{
    private String directoryPath;
    private String databasePath;
    public FolderSelectedEvent(String directoryPath, String databasePath)
    {
        this.directoryPath = directoryPath;
        this.databasePath = databasePath;
    }

    public String getDirectoryPath()
    {
        return directoryPath;
    }

    public String getDatabasePath()
    {
        return databasePath;
    }
    @Override
    public String getMessage() {
        return "Folder selected";
    }
}
