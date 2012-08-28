
package com.pekall.pctool.model;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.contact.Contact.ContactVersion;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {

    private static final String TABLE_CONTACT_VERSIONS = "contact_versions";

    private static final String[] DEFAULT_COLUMNS = {
            ContactVersion._ID, 
            ContactVersion.VERSION
    };

    private DatabaseOpenHelper mDatabaseOpenHelper;
    private SQLiteDatabase mDb;
    private Context mContext;

    public DatabaseHelper(Context context) {
        mContext = context;
        mDatabaseOpenHelper = new DatabaseOpenHelper(mContext);
    }

    public void open() {
        if (mDb == null) {
            mDb = mDatabaseOpenHelper.getWritableDatabase();
        }
    }

    public void close() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    public boolean isOpen() {
        if (mDb == null) {
            return false;
        }

        return mDb.isOpen();
    }

    public Map<Long, Integer> getLastSyncContactVersion() {
        if (!isOpen()) {
            throw new IllegalStateException("database is not open");
        }
        
        Cursor cursor = mDb.query(TABLE_CONTACT_VERSIONS, DEFAULT_COLUMNS, null, null, null, null, null);
        
        Map<Long, Integer> contactVersionDict = Collections.emptyMap();
        
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final int count = cursor.getCount();
                contactVersionDict = new HashMap<Long, Integer>(count);
                
                final int idxOfId = cursor.getColumnIndex(ContactVersion._ID);
                final int idxOfVersion = cursor.getColumnIndex(ContactVersion.VERSION);
                
                do {
                    contactVersionDict.put(cursor.getLong(idxOfId), cursor.getInt(idxOfVersion));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return contactVersionDict;
    }
    
    public boolean updateContactVersions(List<ContactVersion> contactVersions) {
        if (!isOpen()) {
            throw new IllegalStateException("database is not open");
        }
        
        mDb.beginTransaction();
        try {
            // delete all old rows
            mDb.delete(TABLE_CONTACT_VERSIONS, null, null);
            
            final String insertSQL = "insert into " + TABLE_CONTACT_VERSIONS + "(" + 
                    ContactVersion._ID + ", " + 
                    ContactVersion.VERSION + 
                    ") values(?, ?);";
            
            SQLiteStatement insert = mDb.compileStatement(insertSQL);
            
            
            for (ContactVersion contactVersion : contactVersions) {
                insert.bindLong(1, contactVersion.id);
                insert.bindLong(2, contactVersion.version);
                
                insert.executeInsert();
            }
            
            mDb.setTransactionSuccessful();
            return true;
        } catch (SQLiteException e) {
            Slog.e("Error when updateContactVersions", e);
            return false;  
        } finally {
            mDb.endTransaction();
        }
        
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {
        private static final String EXEC_SQL_PREFIX = "SQL EXEC -- ";

        private static final String DB_NAME = "pctool.db";
        private static final int DB_VERSION = 1;

        private static final String CREATE_TABLE_SQL = "CREATE TABLE " +
                TABLE_CONTACT_VERSIONS + "(" + 
                ContactVersion._ID + " INTEGER PRIMARY KEY, " +
                ContactVersion.VERSION + " INTEGER);";

        private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + TABLE_CONTACT_VERSIONS + ";";

        public DatabaseOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                Slog.d(EXEC_SQL_PREFIX + CREATE_TABLE_SQL);
                db.execSQL(CREATE_TABLE_SQL);
            } catch (SQLException e) {
                Slog.e("Error when create tables", e);
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String msg = "Upgrading database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data";
            Slog.w(msg);
            try {
                Slog.d(EXEC_SQL_PREFIX + DROP_TABLE_SQL);
                db.execSQL(DROP_TABLE_SQL);
            } catch (SQLException e) {
                Slog.e("Error when drop tables", e);
            }

            onCreate(db);
        }
    }
}
