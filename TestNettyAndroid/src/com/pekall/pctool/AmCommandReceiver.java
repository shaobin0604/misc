package com.pekall.pctool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AmCommandReceiver extends BroadcastReceiver {
    public static final String ACTION_MAIN_SERVER_START = "com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_START";
    public static final String ACTION_MAIN_SERVER_STOP  = "com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_STOP";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Slog.d("onReceive action = " + action);
		if (ACTION_MAIN_SERVER_START.equalsIgnoreCase(action)) {
		    ServerController.startHttpService(context, /* usbMode */ true);
		    ServerController.startFTPService(context);
        } else if (ACTION_MAIN_SERVER_STOP.equalsIgnoreCase(action)) {
            ServerController.stopFTPService(context);
            ServerController.stopHttpService(context);
        }
	}
}
