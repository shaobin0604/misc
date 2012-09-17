package com.pekall.pctool;

import android.content.Context;
import android.content.Intent;

import org.swiftp.FTPServerService;

public class ServerController {
    
    private ServerController() {
        
    }
    
    public static void startFTPServer(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        if (!FTPServerService.isRunning()) {
            context.startService(serverService);
        }
    }

    public static void stopFTPServer(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        context.stopService(serverService);
    }

    public static void stopHttpServer(Context context) {
        context.stopService(new Intent(context, HttpServerService.class));
    }

    public static void startHttpServer(Context context) {
        context.startService(new Intent(context, HttpServerService.class));
    }
}
