
package com.pekall.pctool;

import static com.pekall.pctool.ServerController.ACTION_SERVER_STATE_CHANGED;
import static com.pekall.pctool.ServerController.EXTRAS_STATE_KEY;
import static com.pekall.pctool.ServerController.STATE_CONNECTED;
import static com.pekall.pctool.ServerController.STATE_DISCONNECTED;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pekall.pctool.ui.MainActivity;
import com.pekall.pctool.util.Slog;

public class ServerStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Slog.d("action: " + action);
        if (ACTION_SERVER_STATE_CHANGED.equals(action)) {
            Bundle extras = intent.getExtras();
            int state = extras.getInt(EXTRAS_STATE_KEY);

            switch (state) {
                case STATE_CONNECTED: 
                case STATE_DISCONNECTED: {
                    int noteTickerResId = 0;
                    int noteTitleResId = 0;
                    int noteTextResId = 0;
                    if (state == STATE_CONNECTED) {
                        noteTickerResId = ServerController.isUsbMode() ? R.string.note_ticker_usb_connected
                                : R.string.note_ticker_wifi_connected;
                        noteTitleResId = ServerController.isUsbMode() ? R.string.note_title_usb_start
                                : R.string.note_title_wifi_start;
                        noteTextResId = ServerController.isUsbMode() ? R.string.note_text_usb_connected
                                : R.string.note_text_wifi_connected;
                    } else if (state == STATE_DISCONNECTED) {
                        noteTickerResId = R.string.note_ticker_wifi_start;
                        noteTitleResId = R.string.note_title_wifi_start;
                        noteTextResId = R.string.note_text_wifi_start;
                    }

                    Notification notification = new Notification();
                    notification.icon = R.drawable.ic_launcher;
                    notification.tickerText = context.getString(noteTickerResId, ServerController.getHostname());
                    notification.when = System.currentTimeMillis();

                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                    notification.setLatestEventInfo(context, context.getString(noteTitleResId),
                            context.getString(noteTextResId, ServerController.getHostname()), contentIntent);

                    NotificationManager notificationManager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(ServerController.NOTIFICATION_ID, notification);
                 
                    break;
                }
                default: {
                    break;
                }
                    
            }
        }
    }
}
