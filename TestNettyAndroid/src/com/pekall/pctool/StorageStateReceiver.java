package com.pekall.pctool;

import com.pekall.pctool.util.Slog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StorageStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Slog.d("action: " + action);
		
		if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
			
		} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
			
		}
		
	}

}
