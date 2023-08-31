package com.bk.modelcreator;

public class UploadFinishedEvent implements Event{
    private String cloudBucketName;
    public UploadFinishedEvent(String cloudBucketName)
    {
        this.cloudBucketName = cloudBucketName;
    }
    @Override
    public String getMessage() {
        return "Images uploaded to " + cloudBucketName;
    }
}
