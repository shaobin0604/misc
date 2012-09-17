
package com.pekall.pctool.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pekall.pctool.App;
import com.pekall.pctool.R;
import com.pekall.pctool.ServiceController;

public class MainActivity extends Activity implements OnClickListener {
    
    private TextView mTvStatus;
    private ToggleButton mTbStatus;
    private App mApp;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mTbStatus = (ToggleButton) findViewById(R.id.tb_status);
        
        mTbStatus.setOnClickListener(this);
        
        mApp = (App) getApplication();
    }

    @Override
    public void onClick(View v) {
        if (v == mTbStatus) {
            if (mTbStatus.isChecked()) {
                ServiceController.startHttpService(mApp);
                ServiceController.startFTPService(mApp);
                ServiceController.startWifiBroadcastService(mApp);
            } else {
                ServiceController.stopWifiBroadcastService(mApp);
                ServiceController.stopFTPService(mApp);
                ServiceController.stopHttpService(mApp);
            }
        }
    }
}
