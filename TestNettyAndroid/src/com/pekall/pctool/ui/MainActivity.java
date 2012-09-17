
package com.pekall.pctool.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pekall.pctool.R;
import com.pekall.pctool.ServerController;

public class MainActivity extends Activity implements OnClickListener {
    
    private TextView mTvStatus;
    private ToggleButton mTbStatus;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mTbStatus = (ToggleButton) findViewById(R.id.tb_status);
        
        
    }

    @Override
    public void onClick(View v) {
        if (v == mTbStatus) {
            if (mTbStatus.isChecked()) {
                // start server in wifi mode
            } else {
                // stop server
                ServerController.stopHttpServer(getApplication());
            }
        }
    }
}
