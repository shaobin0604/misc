package com.pekall.pctool;

import android.content.Context;
import android.content.Intent;

import org.swiftp.FTPServerService;

public class ServiceController {
    
    private ServiceController() {
        
    }
    
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

    public static void startHttpService(Context context, boolean usbMode) {
        final Intent intent = new Intent(context, HttpServerService.class);
        if (usbMode) {
            intent.putExtra(PcToolApp.EXTRAS_USB_MODE, true);
        }
        context.startService(intent);
    }
    
    public static void stopHttpService(Context context) {
        context.stopService(new Intent(context, HttpServerService.class));
    }

    public static void startWifiBroadcastService(Context context) {
        context.startService(new Intent(context, WifiBroadcastService.class));
    }
    
    public static void stopWifiBroadcastService(Context context) {
        context.stopService(new Intent(context, WifiBroadcastService.class));
    }
}
