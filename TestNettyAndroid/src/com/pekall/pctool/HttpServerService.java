
package com.pekall.pctool;

import static com.pekall.pctool.ServerController.ACTION_SERVER_STATE_CHANGED;
import static com.pekall.pctool.ServerController.EXTRAS_STATE_KEY;
import static com.pekall.pctool.ServerController.STATE_START;
import static com.pekall.pctool.ServerController.STATE_STOP;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.pekall.pctool.ui.MainActivity;
import com.pekall.pctool.util.Slog;
import com.pekall.pctool.util.VersionUtil;

public class HttpServerService extends Service {

    private HttpServer mHttpServer;
    
    private BroadcastReceiver mUsbUnPlugReceiver;
    private IntentFilter mUsbUnPlugFilter;
    
    private BroadcastReceiver mStorageStateReceiver;
    private IntentFilter mStorageStateFilter;
    
    private WakeLock mWakeLock;
    private WifiLock mWifiLock;

    @Override
    public void onCreate() {
        Slog.d("onCreate E");
        super.onCreate();

        Slog.d(String.format("PekallPhoneManager, versionName: %s, versionCode: %d", VersionUtil.getVersionName(this),
                VersionUtil.getVersionCode(this)));

        Slog.d("onCreate X");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mHttpServer == null) {
            mHttpServer = new HttpServer(this);
            mHttpServer.start();

            final boolean isUsbMode = intent.getBooleanExtra(ServerController.EXTRAS_USB_MODE, false);
            ServerController.setServiceState(STATE_START);
            ServerController.setUsbMode(isUsbMode);
            
            if (isUsbMode) {
                startListenUsbUnPlugEvent();
            } else {
                acquireWifiLock();
            }

            acquireWakeLock();
            
            startListenStorageState();

            int noteTickerResId = isUsbMode ? R.string.note_ticker_usb_start : R.string.note_ticker_wifi_start;
            int noteTitleResId = isUsbMode ? R.string.note_title_usb_start : R.string.note_title_wifi_start;
            int noteTextResId = isUsbMode ? R.string.note_text_usb_start : R.string.note_text_wifi_start;

            Notification notification = new Notification();
            notification.icon = R.drawable.notification_icon;
            notification.tickerText = getString(noteTickerResId);
            notification.when = System.currentTimeMillis();
            

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            notification.setLatestEventInfo(this, getString(noteTitleResId), getString(noteTextResId), contentIntent);

            startForeground(ServerController.NOTIFICATION_ID, notification);

            Intent serverStateStartIntent = new Intent(ACTION_SERVER_STATE_CHANGED);
            serverStateStartIntent.putExtra(EXTRAS_STATE_KEY, STATE_START);

            sendBroadcast(serverStateStartIntent);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Slog.d("onDestroy E");

        super.onDestroy();

        ServerController.setServiceState(STATE_STOP);
        ServerController.setWifiSecret(null);
        ServerController.setHostname(null);
        
        if (mHttpServer != null) {
            mHttpServer.stop();
            mHttpServer = null;
        }

        if (ServerController.isUsbMode()) {
            stopListenUsbUnPlugEvent();
        } else {
            releaseWifiLock();
        }

        releaseWakeLock();
        
        stopListenStorageState();

        Intent serverStateStopIntent = new Intent(ACTION_SERVER_STATE_CHANGED);
        serverStateStopIntent.putExtra(EXTRAS_STATE_KEY, STATE_STOP);
        sendBroadcast(serverStateStopIntent);

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

    private static class UsbUnPlugEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d("action: " + action);

            if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                ServerController.stopHttpService(context);
                ServerController.stopFTPService(context);
            }
        }

    }
    
    private void startListenStorageState() {
    	 Slog.d("startListenStorageState E");
         mStorageStateReceiver = new StorageStateReceiver();
         
         mStorageStateFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
         mStorageStateFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
         mStorageStateFilter.addDataScheme("file");
         
         registerReceiver(mStorageStateReceiver, mStorageStateFilter);
         Slog.d("startListenStorageState X");
    }
    
    private void stopListenStorageState() {
    	Slog.d("stopListenStorageState E");
    	
    	unregisterReceiver(mStorageStateReceiver);
    	
    	Slog.d("stopListenStorageState X");
    }
    
    private static class StorageStateReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		final String action = intent.getAction();
    		Slog.d("action: " + action);
    		
    		if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
    			ServerController.startFTPService(context);
    		} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
    			ServerController.stopFTPService(context);
    		}
    		
    	}

    }

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            Slog.d("Acquiring wake lock");
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
            mWakeLock.acquire();
            Slog.d("Acquired wake lock");
        }

    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            Slog.d("Releasing wake lock");
            mWakeLock.release();
            mWakeLock = null;
            Slog.d("Released wake lock");
        }
    }

    private void acquireWifiLock() {
        if (mWifiLock == null) {
            Slog.d("Acquiring wifi lock");
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            mWifiLock = wifiManager.createWifiLock("HttpServerService");
            mWifiLock.acquire();
            Slog.d("Acquired wifi lock");
        }

    }

    private void releaseWifiLock() {
        if (mWifiLock != null && mWifiLock.isHeld()) {
            Slog.d("Releasing wifi lock");
            mWifiLock.release();
            mWifiLock = null;
            Slog.d("Released wifi lock");
        }
    }
}
