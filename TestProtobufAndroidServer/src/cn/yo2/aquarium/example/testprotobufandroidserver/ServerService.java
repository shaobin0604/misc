package cn.yo2.aquarium.example.testprotobufandroidserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

public class ServerService extends Service {
    
    private static final int SERVER_PORT = 12580;
    private MainServer mMainServer;
    
    @Override
    public void onCreate() {
        MyLog.d("onCreate E");
        super.onCreate();
        try {
            mMainServer = new MainServer(SERVER_PORT);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
