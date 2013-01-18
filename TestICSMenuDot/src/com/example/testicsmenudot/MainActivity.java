
package com.example.testicsmenudot;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

    private static final int FLAG_NEEDS_MENU_KEY = 0x08000000;
    
    private ListView mLvMainList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~FLAG_NEEDS_MENU_KEY;
        getWindow().setAttributes(attrs); 
        
        setContentView(R.layout.activity_main);
        
        mLvMainList = (ListView) findViewById(android.R.id.list);
        
//        AlarmAdapter adapter = new AlarmAdapter(this);
        
        Cursor cursor = getContentResolver().query(Contacts.CONTENT_URI, null, null, null, null);
        
        AlarmCursorAdapter adapter = new AlarmCursorAdapter(this, cursor);
        
        mLvMainList.setAdapter(adapter);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }
    
    private static final class AlarmCursorAdapter extends CursorAdapter {

        private LayoutInflater mLayoutInflater;
        
        public AlarmCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mLayoutInflater = LayoutInflater.from(context);
        }
        
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mLayoutInflater.inflate(R.layout.list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // TODO Auto-generated method stub
            
        }
        
    }

    private static final class AlarmAdapter extends BaseAdapter {
        
        private LayoutInflater mLayoutInflater;
        
        public AlarmAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 10;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_item, parent, false);
            }
            return convertView;
        }
        
    }
}
