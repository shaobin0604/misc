package com.pekall.popuptest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

public class TimerService extends Service {
	private static final String TAG = TimerService.class.getSimpleName();
	
	private BroadcastReceiver mTimeTickReceiver = new TimeTickReceiver();

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "TimerService#onCreate");
		
		registerReceiver(mTimeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "TimerService#onStartCommand");
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(TAG, "TimerService#onDestroy");
		
		unregisterReceiver(mTimeTickReceiver);
	}
	
	private class TimeTickReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
				Log.d(TAG, "TimeTickReceiver#onReceive ACTION_TIME_TICK");
				
				Intent newIntent = new Intent(context, Popup.class);
				newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				newIntent.putExtra("time", DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis()));
				
				context.startActivity(newIntent);
			}
		}
		
	}
}
