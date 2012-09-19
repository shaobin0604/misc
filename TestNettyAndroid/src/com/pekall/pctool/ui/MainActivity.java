
package com.pekall.pctool.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.pekall.pctool.HttpServerService;
import com.pekall.pctool.PcToolApp;
import com.pekall.pctool.R;
import com.pekall.pctool.ServiceController;
import com.pekall.pctool.Slog;
import com.pekall.pctool.WifiModeUtil;

public class MainActivity extends Activity implements OnClickListener {
    private static final int FRAME_USB = 0;
    private static final int FRAME_WIFI = 1;
    
    private ViewFlipper mViewFlipper;
    
    // usb mode
    private TextView mTvUsbStatus;
    private ToggleButton mTbUsbStatus;
    
    // wifi mode
    private TextView mTvWifiSecret;
    private TextView mTvWifiStatus;
    private ToggleButton mTbWifiStatus;
    
    private PcToolApp mApp;
    
    private boolean mDisplayUsbMode; 
    
    private BroadcastReceiver mServerStateReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d("action: " + action);
            if (HttpServerService.ACTION_SERVER_STATE_START.equals(action)) {
                
            } else if (HttpServerService.ACTION_SERVER_STATE_STOP.equals(action)) {
                finish();
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mApp = (PcToolApp) getApplication();
        
        
        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        
        mTvUsbStatus = (TextView) findViewById(R.id.tv_usb_status);
        mTbUsbStatus = (ToggleButton) findViewById(R.id.tb_usb_status);
        
        mTbUsbStatus.setOnClickListener(this);
        
        mTvWifiSecret = (TextView) findViewById(R.id.tv_wifi_secret);
        mTvWifiStatus = (TextView) findViewById(R.id.tv_wifi_status);
        mTbWifiStatus = (ToggleButton) findViewById(R.id.tb_wifi_status);
        
        mTbWifiStatus.setOnClickListener(this);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        Slog.d("onStart E");
        
        if (mApp.isInService()) {
            mDisplayUsbMode = mApp.isUsbMode();
            
            if (mDisplayUsbMode) {
                mTvUsbStatus.setText(R.string.text_usb_in_service);
                mTbUsbStatus.setChecked(true);
            } else {
                mTvWifiStatus.setText(R.string.text_wifi_in_service);
                mTvWifiSecret.setText(getString(R.string.text_password, mApp.getWifiSecret()));
                mTvWifiSecret.setVisibility(View.VISIBLE);
                
                mTbWifiStatus.setChecked(true);
            }
            
        } else {
            mDisplayUsbMode = false;
            
            mTvWifiStatus.setText(R.string.text_wifi_tips);
            mTvWifiSecret.setVisibility(View.INVISIBLE);
            
            mTbWifiStatus.setChecked(false);
        }
        
        mViewFlipper.setDisplayedChild(mDisplayUsbMode ? FRAME_USB : FRAME_WIFI);
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HttpServerService.ACTION_SERVER_STATE_START);
        intentFilter.addAction(HttpServerService.ACTION_SERVER_STATE_STOP);
        
        registerReceiver(mServerStateReceiver, intentFilter);
        
        Slog.d("onStart X");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Slog.d("onStop E");
        unregisterReceiver(mServerStateReceiver);
        Slog.d("onStop X");
    }

    @Override
    public void onClick(View v) {
        if (v == mTbWifiStatus) {
            if (mTbWifiStatus.isChecked()) {
                ServiceController.startHttpService(mApp, /* usbMode */ false);
                ServiceController.startFTPService(mApp);
                
                String wifiSecret = WifiModeUtil.getWifiHostAddressBase64(mApp);
                
                mApp.setWifiSecret(wifiSecret);
                
                mTvWifiStatus.setText(R.string.text_wifi_in_service);
                mTvWifiSecret.setText(getString(R.string.text_password, wifiSecret));
                mTvWifiSecret.setVisibility(View.VISIBLE);
            } else {
                ServiceController.stopFTPService(mApp);
                ServiceController.stopHttpService(mApp);
                mApp.clearWifiSecret();
            }
        } else if (v == mTbUsbStatus) {
            if (!mTbUsbStatus.isChecked()) {
                ServiceController.stopFTPService(mApp);
                ServiceController.stopHttpService(mApp);
            }
        }
    }
}
