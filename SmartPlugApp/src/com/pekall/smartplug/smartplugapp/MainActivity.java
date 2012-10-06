package com.pekall.smartplug.smartplugapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.pekall.smartplug.model.SmartPlug;
import com.pekall.smartplug.model.SmartPlug.SmartPlugListener;
import com.pekall.smartplug.model.SmartPlugImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements OnClickListener, SmartPlugListener {
//    private static final String HOST = "zealot.eicp.net";
//    private static final int PORT = 10854;
    private static final String HOST = "192.168.1.104";
    private static final int PORT = 16668;
    
    private static final String DEVICE_ID = "1";
    private static final String DEVICE_MODE = "1";
    
    private static final int WHAT_DISCONNECTED = 1;
    private static final int WHAT_SET_STATUS = 2;

    private Lamp mLamp;
    private ToggleButton mNetworkSwitch;
    private ToggleButton mLightSwitch;
    
    private SmartPlug mSmartPlug;
    
    private ExecutorService mExecutorService;
    
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DISCONNECTED:
                    Toast.makeText(MainActivity.this, "disconnected", Toast.LENGTH_SHORT).show();
                    mNetworkSwitch.setChecked(false);
                    break;
                case WHAT_SET_STATUS:
                    boolean status = (msg.arg1 == 1);
                    mLamp.setState(status);
                    mLightSwitch.setChecked(status);
                    break;
                default:
                    break;
            }
        }
        
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mLamp = (Lamp) findViewById(R.id.lamp1);
        
        mNetworkSwitch = (ToggleButton) findViewById(R.id.network_switch);
        mLightSwitch = (ToggleButton) findViewById(R.id.light_switch);
        
        mNetworkSwitch.setOnClickListener(this);
        mLightSwitch.setOnClickListener(this);
        
        mSmartPlug = new SmartPlugImpl(this);
        
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    
    @Override
    public boolean onSetStatusRequested(SmartPlug smartPlug, final boolean on) {
        Message msg = mHandler.obtainMessage(WHAT_SET_STATUS, (on ? 1 : 0), 0);
        return mHandler.sendMessage(msg);
    }



    @Override
    public boolean onGetStatusRequested(SmartPlug smartPlug) {
        return mLamp.getState();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (mSmartPlug.isConnected()) {
            mSmartPlug.disconnect();
        }
        
        mSmartPlug.release();
        
        mExecutorService.shutdown();
    }


    @Override
    public void onError(SmartPlug smartPlug, String msg) {
        // TODO Auto-generated method stub
        
    }



    @Override
    public void onDisconnected(SmartPlug smartPlug) {
        mHandler.sendEmptyMessage(WHAT_DISCONNECTED);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mNetworkSwitch) {
            if (mNetworkSwitch.isChecked()) {
                login(HOST, PORT, DEVICE_ID, DEVICE_MODE);
            } else {
                mSmartPlug.disconnect();
            }
        } else if (v == mLightSwitch) {
            final boolean status = mLightSwitch.isChecked();
            mLamp.setState(status);
            reportStatus(status);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void reportStatus(boolean status) {
        mExecutorService.execute(new ReportStateTask(status));
    }
    
    private void login(String host, int port, String deviceId, String deviceMode) {
        new LoginTask(host, port, deviceId, deviceMode).execute((Void)null);
    }
    
    private class ReportStateTask implements Runnable {
        private boolean mStatus;
        
        public ReportStateTask(boolean status) {
            mStatus = status;
        }
        
        @Override
        public void run() {
            mSmartPlug.reportStatus(mStatus);
        }
        
    }
    
    
    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        
        private String mHost;
        private int mPort;
        private String mDeviceId;
        private String mDeviceMode;
        
        public LoginTask(String host, int port, String deviceId, String deviceMode) {
            mHost = host;
            mPort = port;
            mDeviceId = deviceId;
            mDeviceMode = deviceMode;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean ret = mSmartPlug.connect(mHost, mPort);
            
            Slog.d("connect " + (ret ? "ok" : "fail"));
            
            if (!ret) {
                return false;
            }
            
            ret = mSmartPlug.login(mDeviceId, mDeviceMode);
            
            Slog.d("login " + (ret ? "ok" : "fail"));
            
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mNetworkSwitch.setEnabled(true);
            mNetworkSwitch.setChecked(result);
        }

        @Override
        protected void onPreExecute() {
            mNetworkSwitch.setEnabled(false);
        }
        
    }
}
