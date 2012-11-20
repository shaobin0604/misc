
package com.example.contactimporter;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener {
    private static final String LENOVO_S868T_LOCAL_ACCOUNT_NAME = "contacts.account.name.local";
    private static final String LENOVO_S868T_LOCAL_ACCOUNT_TYPE = "contacts.account.type.local";

    private ToggleButton mTbStart;
    private TextView mTvProgress;

    private ImportTask mImportTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTbStart = (ToggleButton) findViewById(R.id.start_import);
        mTbStart.setOnClickListener(this);
        
        mTvProgress = (TextView) findViewById(R.id.progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == mTbStart) {
            if (mTbStart.isChecked()) {
                startImport();
            } else {
                stopImport();
            }
        }
    }

    private void stopImport() {
        if (mImportTask != null && mImportTask.getStatus() != Status.FINISHED) {
            mImportTask.cancel(true);
        }
    }

    private void startImport() {
        mImportTask = new ImportTask();
        mImportTask.execute((Void) null);
    }

    private class ImportTask extends AsyncTask<Void, Integer, Boolean> {

        private boolean addContact(String email) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, LENOVO_S868T_LOCAL_ACCOUNT_TYPE)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, LENOVO_S868T_LOCAL_ACCOUNT_NAME)
                    .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED).build());

            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, Email.TYPE_WORK).build());

            ContentProviderResult[] results;
            try {
                results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                // insert RawContact result
                ContentProviderResult result = results[0];
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            } catch (OperationApplicationException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            AssetManager am = getAssets();
            try {
                InputStream is = am.open("mail_accounts.dat");
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    if (!isCancelled()) {
                        publishProgress(++count);
                        addContact(line.trim());
                    }
                }
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mTvProgress.setText(String.format("importing %d", values[0]));
        }


        @Override
        protected void onPostExecute(Boolean result) {
            mTvProgress.setText(String.format("import %s", result ? "success": "failed"));
        }
        
        @Override
        protected void onCancelled(Boolean result) {
            mTvProgress.setText("import cancelled");
        }
        
    }
}
