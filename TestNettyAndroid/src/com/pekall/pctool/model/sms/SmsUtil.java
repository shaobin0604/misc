
package com.pekall.pctool.model.sms;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
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

    private static final int INVALID_ROW_ID = 0;
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

    private static final String SMS_ALL = "content://sms";
    private static final String SMS_INBOX = "content://sms/inbox";
    private static final String SMS_SENT = "content://sms/sent";
    private static final String SMS_OUTBOX = "content://sms/outbox";
    private static final String SMS_DRAFT = "content://sms/draft";
    private static final String SMS_FAIL = "cotent://sms/failed";
    private static final String SMS_QUEUED = "content://sms/queued";

    private static final Uri SMS_ALL_URI = Uri.parse(SMS_ALL);
    private static final Uri SMS_INBOX_URI = Uri.parse(SMS_INBOX);
    private static final Uri SMS_SENT_URI = Uri.parse(SMS_SENT);
    private static final Uri SMS_OUTBOX_URI = Uri.parse(SMS_OUTBOX);
    private static final Uri SMS_DRAFT_URI = Uri.parse(SMS_DRAFT);
    private static final Uri SMS_FAIL_URI = Uri.parse(SMS_FAIL);
    private static final Uri SMS_QUEUED_URI = Uri.parse(SMS_QUEUED);

    private static final String DEFAULT_SORT_ORDER = "date DESC";

    // prevent this class being instantiated
    private SmsUtil() {
    }

    /**
     * Get {@link Sms} list
     * 
     * @param context
     * @return List of {@link Sms}
     */
    public static List<Sms> querySmsList(Context context) {
        List<Sms> smsList = new ArrayList<Sms>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(SMS_ALL_URI, null, null, null, DEFAULT_SORT_ORDER);

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
        return context.getContentResolver().delete(Uri.parse(SMS_ALL + "/" + rowId), null, null) > 0;
    }

    public static boolean deleteSmsAll(Context context) {
        return context.getContentResolver().delete(SMS_ALL_URI, null, null) > 0;
    }

    public static boolean deleteSms(Context context, List<Long> rowIds) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for (long rowId : rowIds) {
            ops.add(ContentProviderOperation.newDelete(Uri.parse(SMS_ALL + "/" + rowId)).build());
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
    public static long importSms(Context context, Sms sms) {
        // if (PhoneNumberUtils.isGlobalPhoneNumber(sms.address)) {
        // Slog.e("address invalid: " + sms.address);
        // return false;
        // }

        ContentValues values = new ContentValues();
        values.put(ADDRESS, sms.address);
        /** one phoneNum only has one draft sms, discard exceed body **/
        values.put(BODY, sms.body);
        values.put(DATE, sms.date);
        Uri newSmsUri = context.getContentResolver().insert(typeToUri(sms.type), values);

        Slog.d("newSmsUri = " + newSmsUri);

        if (newSmsUri != null) {
            return Long.valueOf(newSmsUri.getPathSegments().get(0));
        } else {
            return INVALID_ROW_ID;
        }
    }

    private static final Uri typeToUri(int type) {
        switch (type) {
            case Sms.TYPE_RECEIVED:
                return SMS_INBOX_URI;
            case Sms.TYPE_SENT:
                return SMS_SENT_URI;
            case Sms.TYPE_DRAFT:
                return SMS_DRAFT_URI;
            case Sms.TYPE_OUTBOX:
                return SMS_OUTBOX_URI;
            case Sms.TYPE_FAILED:
                return SMS_FAIL_URI;
            case Sms.TYPE_QUEUED:
                return SMS_QUEUED_URI;
            default:
                throw new IllegalArgumentException("Error invalid sms type: " + type);
        }
    }
}
