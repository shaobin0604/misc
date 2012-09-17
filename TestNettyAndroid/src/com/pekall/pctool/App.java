package com.pekall.pctool;

import android.app.Application;

public class App extends Application {
    
    private boolean mConnectionModeUSB;

    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    public void setConnectionMode(boolean connectionModeUSB) {
        mConnectionModeUSB = connectionModeUSB;
    }

    public boolean isConnectionModeUSB() {
        return mConnectionModeUSB;
    }
}
