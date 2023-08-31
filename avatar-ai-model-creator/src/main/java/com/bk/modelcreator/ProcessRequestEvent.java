package com.bk.modelcreator;

public class ProcessRequestEvent implements Event{
    private String srcDirectory;
    private String outputZipFile;
    public ProcessRequestEvent(String srcDirectory, String outputZipFile)
    {
        this.srcDirectory = srcDirectory;
        this.outputZipFile = outputZipFile;

    }
    public String getSrcDirectory()
    {
        return srcDirectory;
    }

    public String getOutputZipFile()
    {
        return outputZipFile;
    }
    @Override
    public String getMessage() {
        return "Process request initiated";
    }
}
