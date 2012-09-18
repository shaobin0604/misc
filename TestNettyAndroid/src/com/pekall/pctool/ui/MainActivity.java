
package com.pekall.pctool.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pekall.pctool.PcToolApp;
import com.pekall.pctool.R;
import com.pekall.pctool.ServiceController;
import com.pekall.pctool.WifiModeUtil;

public class MainActivity extends Activity implements OnClickListener {
    
    // usb mode
    private TextView mTvUsbStatus;
    private ToggleButton mTbUsbStatus;
    
    // wifi mode
    private TextView mTvWifiSecret;
    private TextView mTvWifiStatus;
    private ToggleButton mTbWifiStatus;
    
    
    private PcToolApp mApp;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mApp = (PcToolApp) getApplication();
        
        mTvUsbStatus = (TextView) findViewById(R.id.tv_usb_status);
        mTbUsbStatus = (ToggleButton) findViewById(R.id.tb_usb_status);
        
        mTbUsbStatus.setOnClickListener(this);
        
        mTvWifiSecret = (TextView) findViewById(R.id.tv_wifi_secret);
        mTvWifiStatus = (TextView) findViewById(R.id.tv_wifi_status);
        mTbWifiStatus = (ToggleButton) findViewById(R.id.tb_wifi_status);
        
        mTbWifiStatus.setOnClickListener(this);
        
    }
    
    

    @Override
    public void onClick(View v) {
        if (v == mTbWifiStatus) {
            if (mTbWifiStatus.isChecked()) {
                ServiceController.startHttpService(mApp, /* usbMode */ false);
                ServiceController.startFTPService(mApp);
                
                String wifiSecret = WifiModeUtil.getWifiHostAddressBase64(mApp);
                
                mApp.setWifiSecret(wifiSecret);
                
                mTvWifiStatus.setText(wifiSecret);
            } else {
                ServiceController.stopFTPService(mApp);
                ServiceController.stopHttpService(mApp);
                
                mApp.clearWifiSecret();
            }
        }
    }
}
