package com.bk.modelcreator;

public class UploadRequestEvent implements Event{
    private String filePath;
    public UploadRequestEvent(String filePath)
    {
        this.filePath = filePath;
    }
    @Override
    public String getMessage() {
        return "Zipped file; now uploading";
    }
    public String getFilePath()
    {
        return filePath;
    }

}
