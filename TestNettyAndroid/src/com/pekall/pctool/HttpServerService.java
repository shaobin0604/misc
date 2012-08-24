package com.pekall.pctool;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class HttpServerService extends Service {
    private HttpServer mHttpServer;
    private BroadcastReceiver mUsbUnPlugReceiver;
    private IntentFilter mUsbUnPlugFilter;
    
    @Override
    public void onCreate() {
        Slog.d("onCreate E");
        super.onCreate();
        mHttpServer = new HttpServer(this);
        mHttpServer.start();
        Slog.d("onCreate X");
        
        mUsbUnPlugReceiver = new UsbUnPlugEventReceiver();
        mUsbUnPlugFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        
        startListenUsbUnPlugEvent();
    }
    
    @Override
    public void onDestroy() {
        Slog.d("onDestroy E");
        super.onDestroy();
        
        if (mHttpServer != null) {
            mHttpServer.stop();
            mHttpServer = null;
        }
        
        stopListenUsbUnPlugEvent();
        
        Slog.d("onDestroy X");
    }
    
    private boolean isServerRunning() {
        if (mHttpServer != null) {
            return mHttpServer.isAlive();
        } else {
            return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void startListenUsbUnPlugEvent() {
        Slog.d("startListenUsbUnPlugEvent E");
        registerReceiver(mUsbUnPlugReceiver, mUsbUnPlugFilter);
        Slog.d("startListenUsbUnPlugEvent X");
    }
    
    private void stopListenUsbUnPlugEvent() {
        Slog.d("stopListenUsbUnPlugEvent E");
        unregisterReceiver(mUsbUnPlugReceiver);
        Slog.d("stopListenUsbUnPlugEvent X");
    }
    
    private class UsbUnPlugEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d("action: " + action);
            
            if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                ServerController.stopHttpServer(context);
                ServerController.stopFTPServer(context);
            }
        }
        
    }
    
}
