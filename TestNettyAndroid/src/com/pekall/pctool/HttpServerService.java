package com.pekall.pctool;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.pekall.pctool.ui.MainActivity;

public class HttpServerService extends Service {
    private HttpServer mHttpServer;
    private BroadcastReceiver mUsbUnPlugReceiver;
    private IntentFilter mUsbUnPlugFilter;
    
    private PcToolApp mApp;
    
    @Override
    public void onCreate() {
        Slog.d("onCreate E");
        super.onCreate();
        mApp = (PcToolApp) getApplication();
        Slog.d("onCreate X");
    }
    
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mHttpServer == null) {
            mHttpServer = new HttpServer(this);
            mHttpServer.start();
            
            final boolean isUsbMode = intent.getBooleanExtra(PcToolApp.EXTRAS_USB_MODE, false);
            mApp.setUsbMode(isUsbMode);
            if (isUsbMode) {
                startListenUsbUnPlugEvent();
            }
            
            int noteTickerResId = isUsbMode ? R.string.note_ticker_usb_start : R.string.note_ticker_wifi_start;
            int noteTitleResId = isUsbMode ? R.string.note_title_usb_start : R.string.note_title_wifi_start;
            int noteTextResId = isUsbMode ? R.string.note_text_usb_start : R.string.note_text_wifi_start;
            
            Notification notification = new Notification();
            notification.icon = R.drawable.ic_launcher;
            notification.tickerText = getString(noteTickerResId);
            notification.when = System.currentTimeMillis();
            
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            
            notification.setLatestEventInfo(this, getString(noteTitleResId), getString(noteTextResId), contentIntent);
                
            startForeground(PcToolApp.NOTIFICATION_ID, notification);
        }
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Slog.d("onDestroy E");
        
        super.onDestroy();
        
        if (mHttpServer != null) {
            mHttpServer.stop();
            mHttpServer = null;
        }
        
        if (mApp.isUsbMode()) {
            stopListenUsbUnPlugEvent();
        }
        
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
        mUsbUnPlugReceiver = new UsbUnPlugEventReceiver();
        mUsbUnPlugFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
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
                ServiceController.stopHttpService(context);
                ServiceController.stopFTPService(context);
            }
        }
        
    }
    
}
