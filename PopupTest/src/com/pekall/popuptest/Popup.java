package com.pekall.popuptest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Popup extends Activity {
	
	
	private static final String TAG = "Popup";
	private TextView mTvTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "Popup#onCreate");
		
		setContentView(R.layout.popup);
		
		mTvTime = (TextView) findViewById(R.id.time);
		
		Intent intent = getIntent();
		
		handleIntent(intent);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Log.d(TAG, "Popup#onNewIntent");
		
		handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		String time = intent.getStringExtra("time");
		
		Log.d(TAG, "Popup#handleIntent time: " + time);
		
		mTvTime.setText(time);
	}
}
