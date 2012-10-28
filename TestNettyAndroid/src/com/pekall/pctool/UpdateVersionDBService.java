
package com.pekall.pctool;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import com.pekall.pctool.model.DatabaseHelper;
import com.pekall.pctool.model.FastSyncUtils;
import com.pekall.pctool.model.calendar.CalendarUtil;
import com.pekall.pctool.model.calendar.EventInfo.EventVersion;
import com.pekall.pctool.model.contact.Contact.ContactVersion;
import com.pekall.pctool.model.contact.ContactUtil;
import com.pekall.pctool.util.Slog;

import java.util.List;

public class UpdateVersionDBService extends IntentService {

    private DatabaseHelper mDatabaseHelper;

    public UpdateVersionDBService() {
        super("update-version-db-thread");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mDatabaseHelper = new DatabaseHelper(this);
        mDatabaseHelper.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDatabaseHelper != null) {
            mDatabaseHelper.close();
            mDatabaseHelper = null;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Slog.d("action = " + action);

        if (FastSyncUtils.ACTION_UPDATE_CONTACT_VERSION.equals(action)) {
            List<ContactVersion> contactVersions = ContactUtil.getPhoneContactVersions(this);
            boolean success = mDatabaseHelper.updateContactVersions(contactVersions);
            if (success) {
                Slog.d("update phone contact version OK");
            } else {
                Slog.e("Error update contact version");
            }
        } else if (FastSyncUtils.ACTION_UPDATE_EVENT_VERSION.equals(action)) {
            List<EventVersion> eventVersions = CalendarUtil.getEventVersions(this);
            boolean success = mDatabaseHelper.updateEventVersions(eventVersions);
            if (success) {
                Slog.d("update event version OK");
            } else {
                Slog.e("Error update event version");
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
