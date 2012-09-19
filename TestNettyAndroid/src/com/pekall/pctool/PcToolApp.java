package com.pekall.pctool;

import android.app.Application;

public class PcToolApp extends Application {
    public static final String EXTRAS_USB_MODE = "pctool.extras.usbmode";
    public static final int NOTIFICATION_ID = R.string.app_name;
    
    private boolean mIsAuthorized;
    private boolean mIsInService;
    private boolean mIsUsbMode;
    private String mWifiSecret;
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    public void setAuthorized(boolean isAuthorized) {
        mIsAuthorized = isAuthorized;
    }
    
    public boolean isAuthorized() {
        return mIsAuthorized;
    }
    
    public void setInService(boolean isInService) {
        mIsInService = isInService;
    }
    
    public boolean isInService() {
        return mIsInService;
    }
    
    public void setUsbMode(boolean isUsbMode) {
        mIsUsbMode = isUsbMode;
    }

    public boolean isUsbMode() {
        return mIsUsbMode;
    }
    
    public void setWifiSecret(String wifiSecret) {
        mWifiSecret = wifiSecret;
    }
    
    public void clearWifiSecret() {
        mWifiSecret = null;
    }
    
    public String getWifiSecret() {
        return mWifiSecret;
    }
    
    public boolean isWifiSecretMatch(String wifiSecret) {
        return wifiSecret.equals(mWifiSecret);
    }
}
