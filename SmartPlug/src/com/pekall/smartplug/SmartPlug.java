package com.pekall.smartplug;

public interface SmartPlug {
    
    public static enum Error {
        ERR_AUTH_FAIL((short)-1),
        ERR_SERVER_ERR((short)-2),
        ERR_SERVER_NOT_RESPONSE((short)-3);
        
        private short mValue;
        
        private Error(short value) {
            mValue = value;
        }
        
        public short getValue() {
            return mValue;
        }
        
        public static Error fromValue(short value) {
            switch (value) {
                case -1:
                    return ERR_AUTH_FAIL;
                case -2:
                    return ERR_SERVER_ERR;
                case -3:
                    return ERR_SERVER_NOT_RESPONSE;
                default:
                    throw new IllegalArgumentException("Unknown value: " + value);
            }
        }
    }
    
    public boolean connect(String host, int port);
    public boolean login(String pn, String sn);
    public boolean reportStatus(boolean on);
    public void disconnect();
    public boolean isConnected(); 
    public void release();
    
    public static interface SmartPlugListener {
        public boolean onSetStatusRequested(SmartPlug smartPlug, boolean on);
        public boolean onGetStatusRequested(SmartPlug smartPlug);
        public void onError(SmartPlug smartPlug, String msg);
        public void onDisconnected(SmartPlug smartPlug);
    }
}


