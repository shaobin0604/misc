package com.pekall.pctool;

import android.content.Context;
import android.content.Intent;

import org.swiftp.FTPServerService;

class ServerController {
    
    private ServerController() {
        
    }
    
    static void startFTPServer(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        if (!FTPServerService.isRunning()) {
            context.startService(serverService);
        }
    }

    static void stopFTPServer(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        context.stopService(serverService);
    }

    static void stopHttpServer(Context context) {
        context.stopService(new Intent(context, HttpServerService.class));
    }

    static void startHttpServer(Context context) {
        context.startService(new Intent(context, HttpServerService.class));
    }
}
