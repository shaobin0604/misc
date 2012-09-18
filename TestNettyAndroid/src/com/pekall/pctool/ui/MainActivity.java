
package com.pekall.pctool.ui;

import android.app.Activity;
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
    
    private TextView mTvStatus;
    private ToggleButton mTbStatus;
    private PcToolApp mApp;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mTbStatus = (ToggleButton) findViewById(R.id.tb_status);
        
        mTbStatus.setOnClickListener(this);
        
        mApp = (PcToolApp) getApplication();
    }

    @Override
    public void onClick(View v) {
        if (v == mTbStatus) {
            if (mTbStatus.isChecked()) {
                ServiceController.startHttpService(mApp, /* usbMode */ false);
                ServiceController.startFTPService(mApp);
                
                String wifiSecret = WifiModeUtil.getWifiHostAddressBase64(mApp);
                
                mApp.setWifiSecret(wifiSecret);
                
                mTvStatus.setText(wifiSecret);
            } else {
                ServiceController.stopFTPService(mApp);
                ServiceController.stopHttpService(mApp);
                
                mApp.clearWifiSecret();
            }
        }
    }
}
