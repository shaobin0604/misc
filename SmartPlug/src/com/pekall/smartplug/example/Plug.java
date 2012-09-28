package com.pekall.smartplug.example;

public class Plug {
    private boolean mStatus;    // true for on, false for off
    
    public boolean setStatus(boolean status) {
        mStatus = status;
        return true;
    }
    
    public boolean getStatus() {
        return mStatus;
    }
}
