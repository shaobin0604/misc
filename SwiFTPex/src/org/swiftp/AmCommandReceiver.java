package org.swiftp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AmCommandReceiver extends BroadcastReceiver {
	
	protected MyLog myLog = new MyLog(getClass().getName());
	
    static public final String ACTION_START_SERVER = "org.swiftp.AmCommandReceiver.START_SERVER";
    static public final String ACTION_STOP_SERVER = "org.swiftp.AmCommandReceiver.STOP_SERVER";
    
    private void startServer(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        if (!FTPServerService.isRunning()) {
            context.startService(serverService);
        }
    }

    private void stopServer(Context context) {
        Intent serverService = new Intent(context, FTPServerService.class);
        context.stopService(serverService);
    }

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		myLog.d("onReceive action = " + action);
		if (ACTION_START_SERVER.equals(action)) {
			startServer(context);
		} else if (ACTION_STOP_SERVER.equals(action)) {
			stopServer(context);
		}
	}

}
