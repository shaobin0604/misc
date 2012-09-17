package com.pekall.pctool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AmCommandReceiver extends BroadcastReceiver {
	
	
    public static final String ACTION_FTP_SERVER_START  = "com.pekall.pctool.AmCommandReceiver.ACTION_FTP_SERVER_START";
    public static final String ACTION_FTP_SERVER_STOP   = "com.pekall.pctool.AmCommandReceiver.ACTION_FTP_SERVER_STOP";
    public static final String ACTION_MAIN_SERVER_START = "com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_START";
    public static final String ACTION_MAIN_SERVER_STOP  = "com.pekall.pctool.AmCommandReceiver.ACTION_MAIN_SERVER_STOP";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Slog.d("onReceive action = " + action);
		if (ACTION_FTP_SERVER_START.equals(action)) {
			ServiceController.startFTPService(context);
		} else if (ACTION_FTP_SERVER_STOP.equals(action)) {
		    ServiceController.stopFTPService(context);
		} else if (ACTION_MAIN_SERVER_START.equalsIgnoreCase(action)) {
		    ServiceController.startHttpService(context);
        } else if (ACTION_MAIN_SERVER_STOP.equalsIgnoreCase(action)) {
            ServiceController.stopHttpService(context);
        }
	}
	
	

}
