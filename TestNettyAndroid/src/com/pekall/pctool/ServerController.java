package com.pekall.pctool;

import android.content.Context;
import android.content.Intent;

import org.swiftp.FTPServerService;

public class ServerController {
    public static final String EXTRAS_USB_MODE = "pctool.extras.usbmode";
    public static final int NOTIFICATION_ID = R.string.app_name;
    
    
    public static final String ACTION_SERVER_STATE_CHANGED = "action.SERVER_STATE_CHANGED";

    public static final String EXTRAS_STATE_KEY = "extras.STATE_KEY";
    // server on/off
    public static final int STATE_START = 1;
    public static final int STATE_STOP = 2;
    
    // client connected/disconnected
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_DISCONNECTED = 4;
    
    // Server state
    private static int sServerState = STATE_STOP;
    
    // Server connection mode: usb or wifi
    private static boolean sIsUsbMode;
    
    // Wifi connection secret, valid when Server is on in wifi mode
    private static String sWifiSecret;
    
    // The connected PC's host name
    private static String sHostname;
    
    private ServerController() {
        
    }
    
    public static void setServiceState(int serviceState) {
        sServerState = serviceState;
    }
    
    public static int getServerState() {
        return sServerState;
    }
    
    public static String getServerStateDisplayString() {
        return serverStateToDisplayString(sServerState);
    }
    
    public static String serverStateToDisplayString(int serverState) {
        switch (serverState) {
            case STATE_START:
                return "STATE_START";
            case STATE_STOP: 
                return "STATE_STOP";
            case STATE_CONNECTED:
                return "STATE_CONNECTED";
            case STATE_DISCONNECTED:
                return "STATE_DISCONNECTED";
            default:
                // WARN: should not goes here
                return "STATE_UNKNOWN";
        }
    }
    
    public static boolean isUsbMode() {
        return sIsUsbMode;
    }

    public static void setUsbMode(boolean isUsbMode) {
        ServerController.sIsUsbMode = isUsbMode;
    }

    public static String getWifiSecret() {
        return sWifiSecret;
    }

    public static void setWifiSecret(String wifiSecret) {
        if (wifiSecret != null) {
            wifiSecret = wifiSecret.trim();
        }
        ServerController.sWifiSecret = wifiSecret;
    }

    public static String getHostname() {
        return sHostname;
    }

    public static void setHostname(String hostname) {
        ServerController.sHostname = hostname;
    }

    public static void sendServerStateBroadcast(Context context, int state) {
        Intent intent = new Intent(ACTION_SERVER_STATE_CHANGED);
        intent.putExtra(EXTRAS_STATE_KEY, state);
        
        context.sendBroadcast(intent);
    }
    
    //
    // FTP
    //
    
    public static void startFTPService(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        if (!FTPServerService.isRunning()) {
            context.startService(serverService);
        }
    }

    public static void stopFTPService(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        context.stopService(serverService);
    }

    //
    // HTTP
    //
    
    public static void startHttpService(Context context, boolean usbMode) {
        final Intent intent = new Intent(context, HttpServerService.class);
        if (usbMode) {
            intent.putExtra(EXTRAS_USB_MODE, true);
        }
        context.startService(intent);
    }
    
    public static void stopHttpService(Context context) {
        context.stopService(new Intent(context, HttpServerService.class));
    }

    //
    // Device discovery broadcast
    //
    
    public static void startWifiBroadcastService(Context context) {
        context.startService(new Intent(context, WifiBroadcastService.class));
    }
    
    public static void stopWifiBroadcastService(Context context) {
        context.stopService(new Intent(context, WifiBroadcastService.class));
    }

    public static boolean isWifiSecretMatch(String wifiSecret) {
        if (wifiSecret == null) {
            return false; 
        }
        
        return wifiSecret.equals(sWifiSecret);
    }
    
}
