package cn.yo2.aquarium.example.testnettyandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainServiceBroadcastReceiver extends BroadcastReceiver {
    private static String START_ACTION = "NotifyServiceStart";
    private static String STOP_ACTION = "NotifyServiceStop";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        
        MyLog.d("action = " + action);
        
        if (START_ACTION.equalsIgnoreCase(action)) {
            context.startService(new Intent(context, MainService.class));
        } else if (STOP_ACTION.equalsIgnoreCase(action)) {
            context.stopService(new Intent(context, MainService.class));
        }
    }

}
