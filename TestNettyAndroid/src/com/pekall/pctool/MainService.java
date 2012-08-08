package com.pekall.pctool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MainService extends Service {
    private MainServer mMainServer;
    
    @Override
    public void onCreate() {
        Slog.d("onCreate E");
        super.onCreate();
        mMainServer = new MainServer(this);
        mMainServer.start();
        Slog.d("onCreate X");
    }
    
    @Override
    public void onDestroy() {
        Slog.d("onDestroy E");
        super.onDestroy();
        
        if (mMainServer != null) {
            mMainServer.stop();
            mMainServer = null;
        }
        Slog.d("onDestroy X");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    
}
