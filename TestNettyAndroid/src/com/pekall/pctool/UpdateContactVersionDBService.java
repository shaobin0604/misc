package com.pekall.pctool;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import com.pekall.pctool.model.DatabaseHelper;
import com.pekall.pctool.model.contact.ContactUtil;
import com.pekall.pctool.model.contact.Contact.ContactVersion;

import java.util.List;

public class UpdateContactVersionDBService extends IntentService {
    
    private DatabaseHelper mDatabaseHelper;

    public UpdateContactVersionDBService() {
        super("update-contact-version-db-thread");
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
        Slog.d("action = " + intent.getAction());
        
        List<ContactVersion> contactVersions = ContactUtil.getAllContactVersions(this);
        boolean success = mDatabaseHelper.updateContactVersions(contactVersions);
        if (success) {
            Slog.d("update contact version OK");
        } else {
            Slog.e("Error update contact version");
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
