package cn.yo2.aquarium.example.testnettyandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MainService extends Service {
    private MainServer mMainServer;
    
    @Override
    public void onCreate() {
        MyLog.d("onCreate E");
        super.onCreate();
        mMainServer = new MainServer();
        mMainServer.start();
        MyLog.d("onCreate X");
    }
    
    @Override
    public void onDestroy() {
        MyLog.d("onDestroy E");
        super.onDestroy();
        
        if (mMainServer != null) {
            mMainServer.stop();
            mMainServer = null;
        }
        MyLog.d("onDestroy X");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    
}
