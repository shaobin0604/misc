package com.pekall.popuptest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

public class PopupTestActivity extends Activity implements OnClickListener {
    private ToggleButton mTimerSwitch;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mTimerSwitch = (ToggleButton) findViewById(R.id.timer_switch);
        
        mTimerSwitch.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		if (v == mTimerSwitch) {
			enableTimerService(mTimerSwitch.isChecked());
		}
	}

	private void enableTimerService(boolean checked) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, TimerService.class);
		
		if (checked) {
			startService(intent);
		} else {
			stopService(intent);
		}
	}
    
    
}