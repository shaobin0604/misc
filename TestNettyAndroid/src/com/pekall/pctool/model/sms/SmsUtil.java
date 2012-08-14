
package com.pekall.pctool.model.sms;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;

import com.pekall.pctool.Slog;

import java.util.ArrayList;
import java.util.List;

public class SmsUtil {

    private static final String ERROR_CODE = "error_code";
    private static final String LOCKED = "locked";
    private static final String SERVICE_CENTER = "service_center";
    private static final String BODY = "body";
    private static final String TYPE = "type";
    private static final String STATUS = "status";
    private static final String READ = "read";
    private static final String PROTOCOL = "protocol";
    private static final String DATE = "date";
    private static final String ADDRESS = "address";
    private static final String _ID = "_id";

    private static final String SMS_AUTHORITY = "sms";

    private static final Uri ALL_URI = Uri.parse("content://sms");
    private static final Uri INBOX_URI = Uri.parse("content://sms/inbox");
    private static final Uri SENT_URI = Uri.parse("content://sms/sent");
    private static final Uri OUTBOX_URI = Uri.parse("content://sms/outbox");
    private static final Uri DRAFT_URI = Uri.parse("content://sms/draft");
    private static final Uri FAIL_URI = Uri.parse("content://sms/failed");
    private static final Uri QUEUED_URI = Uri.parse("content://sms/queued");

    private SmsUtil() {
    }

    /**
     * Get {@link Sms} list
     * 
     * @param context
     * @return List of {@link Sms}
     */
    public static List<Sms> getSmsList(Context context) {
        List<Sms> smsList = new ArrayList<Sms>();
        Cursor cursor = context.getContentResolver().query(ALL_URI, null, null, null, "_id desc");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int rowIdIndex = cursor.getColumnIndex(_ID);
                int addressIndex = cursor.getColumnIndex(ADDRESS);
                int dateIndex = cursor.getColumnIndex(DATE);
                int protocolIndex = cursor.getColumnIndex(PROTOCOL);
                int readIndex = cursor.getColumnIndex(READ);
                int statusIndex = cursor.getColumnIndex(STATUS);
                int typeIndex = cursor.getColumnIndex(TYPE);
                int bodyIndex = cursor.getColumnIndex(BODY);
                int serviceCenterIndex = cursor.getColumnIndex(SERVICE_CENTER);
                int lockedIndex = cursor.getColumnIndex(LOCKED);
                int errorCodeIndex = cursor.getColumnIndex(ERROR_CODE);
                do {
                    Sms sms = new Sms();

                    sms.rowId = cursor.getLong(rowIdIndex);
                    sms.address = cursor.getString(addressIndex);
                    sms.date = cursor.getLong(dateIndex);
                    sms.protocol = cursor.getInt(protocolIndex);
                    sms.read = cursor.getInt(readIndex);
                    sms.status = cursor.getInt(statusIndex);
                    sms.type = cursor.getInt(typeIndex);
                    sms.body = cursor.getString(bodyIndex);
                    String serviceCenter = cursor.getString(serviceCenterIndex);
                    if (serviceCenter == null) {
                        serviceCenter = "";
                    }
                    sms.serviceCenter = serviceCenter;
                    sms.locked = cursor.getInt(lockedIndex);
                    sms.errorCode = cursor.getInt(errorCodeIndex);

                    smsList.add(sms);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        return smsList;
    }

    /**
     * @param context
     * @param rowId
     * @return true if success, otherwise false
     */
    public static boolean deleteSms(Context context, long rowId) {
        return context.getContentResolver().delete(ALL_URI, _ID + "=?", new String[] {
                String.valueOf(rowId)
        }) == 1;
    }

    public static boolean deleteSms(Context context, List<Long> rowIds) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for (long rowId : rowIds) {
            ops.add(ContentProviderOperation.newDelete(ALL_URI)
                    .withSelection(_ID + "=?", new String[] {
                        String.valueOf(rowId)
                    })
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(SMS_AUTHORITY, ops);
            return true;
        } catch (RemoteException e) {
            Slog.e("Error when deleteSms", e);
        } catch (OperationApplicationException e) {
            Slog.e("Error when deleteSms", e);
        }
        
        return false;
    }

    /**
     * @param context
     * @param sms
     * @return
     */
    public static boolean importSms(Context context, Sms sms) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(sms.address)) {
            Slog.e("address invalid: " + sms.address);
            return false;
        }
        
        ContentValues values = new ContentValues();
        values.put(ADDRESS, sms.address);
        /** one phoneNum only has one draft sms, discard exceed body **/
        values.put(BODY, sms.body);
        values.put(DATE, sms.date);
        return context.getContentResolver().insert(typeToUri(sms.type), values) != null;
    }
    
    private static final Uri typeToUri(int type) {
        switch (type) {
            case Sms.TYPE_RECEIVED:
                return INBOX_URI;
            case Sms.TYPE_SENT:
                return SENT_URI;
            case Sms.TYPE_DRAFT:
                return DRAFT_URI;
            case Sms.TYPE_OUTBOX:
                return OUTBOX_URI;
            case Sms.TYPE_FAILED:
                return FAIL_URI;
            case Sms.TYPE_QUEUED:
                return QUEUED_URI;
            default:
                throw new IllegalArgumentException("Error invalid sms type: " + type);
        }
    }
}
