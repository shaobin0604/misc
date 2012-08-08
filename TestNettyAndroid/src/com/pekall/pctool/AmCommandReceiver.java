package com.pekall.pctool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.swiftp.FTPServerService;

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
			startFTPServer(context);
		} else if (ACTION_FTP_SERVER_STOP.equals(action)) {
			stopFTPServer(context);
		} else if (ACTION_MAIN_SERVER_START.equalsIgnoreCase(action)) {
            startMainServer(context);
        } else if (ACTION_MAIN_SERVER_STOP.equalsIgnoreCase(action)) {
            stopMainServer(context);
        }
	}
	
	private void startFTPServer(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        if (!FTPServerService.isRunning()) {
            context.startService(serverService);
        }
    }

    private void stopFTPServer(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        context.stopService(serverService);
    }

    private void stopMainServer(Context context) {
        context.stopService(new Intent(context, MainService.class));
    }

    private void startMainServer(Context context) {
        context.startService(new Intent(context, MainService.class));
    }

}
