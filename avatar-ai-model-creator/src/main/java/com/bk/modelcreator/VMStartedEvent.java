package com.bk.modelcreator;

public class VMStartedEvent implements Event{

    private String ipAddress;
    public VMStartedEvent(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public String getAddress()
    {
        return ipAddress;
    }
    @Override
    public String getMessage() {
        return "VM started";
    }
}
