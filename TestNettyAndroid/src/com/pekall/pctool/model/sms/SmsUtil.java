
package com.pekall.pctool.model.sms;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;

import com.pekall.pctool.model.contact.ContactUtil;
import com.pekall.pctool.util.Slog;

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
    private static final String PERSON = "person";
    private static final String THREAD_ID = "thread_id";
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

    private static final Uri ICC_URI = Uri.parse("content://sms/icc");
    private static final Uri ICC2_URI = Uri.parse("content://sms/icc2"); // this
                                                                         // uri
                                                                         // is
                                                                         // valid
                                                                         // only
                                                                         // on
                                                                         // dual
                                                                         // sim
                                                                         // phone

    private static final String DEFAULT_SORT_ORDER = "date DESC";

    private static final String[] PHONE_SMS_PROJECTION = {
            _ID, THREAD_ID, PERSON, ADDRESS, DATE, PROTOCOL, READ, STATUS, TYPE, BODY, SERVICE_CENTER, LOCKED,
            ERROR_CODE,
    };
    
    private static final String[] SIM_SMS_PROJECTION = {
        _ID, THREAD_ID, ADDRESS, DATE, PROTOCOL, READ, STATUS, TYPE, BODY, SERVICE_CENTER, LOCKED,
        ERROR_CODE,
    };

    // prevent this class being instantiated
    private SmsUtil() {
    }
    
    /**
     * Get {@link Sms} list on Sim2
     * 
     * @param context
     * @return List of {@link Sms}
     */
    public static List<Sms> querySim2SmsList(Context context) {
        List<Sms> smsList = new ArrayList<Sms>();
        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(ICC2_URI, SIM_SMS_PROJECTION, null, null, DEFAULT_SORT_ORDER);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    final int rowIdIndex = cursor.getColumnIndex(_ID);
                    final int threadIdIndex = cursor.getColumnIndex(THREAD_ID);
                    final int addressIndex = cursor.getColumnIndex(ADDRESS);
                    final int dateIndex = cursor.getColumnIndex(DATE);
                    final int protocolIndex = cursor.getColumnIndex(PROTOCOL);
                    final int readIndex = cursor.getColumnIndex(READ);
                    final int statusIndex = cursor.getColumnIndex(STATUS);
                    final int typeIndex = cursor.getColumnIndex(TYPE);
                    final int bodyIndex = cursor.getColumnIndex(BODY);
                    final int serviceCenterIndex = cursor.getColumnIndex(SERVICE_CENTER);
                    final int lockedIndex = cursor.getColumnIndex(LOCKED);
                    final int errorCodeIndex = cursor.getColumnIndex(ERROR_CODE);

                    do {
                        Sms sms = new Sms();

                        sms.rowId = cursor.getLong(rowIdIndex);
                        sms.threadId = cursor.getLong(threadIdIndex);
                        sms.address = cursor.getString(addressIndex);
                        if (sms.person <= 0) {
                            sms.person = ContactUtil.getRawContactId(context, sms.address);
                        }
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
            } finally {
                cursor.close();
            }
        }

        return smsList;
    }

    /**
     * Get {@link Sms} list on Sim1
     * 
     * @param context
     * @return List of {@link Sms}
     */
    public static List<Sms> querySim1SmsList(Context context) {
        List<Sms> smsList = new ArrayList<Sms>();
        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(ICC_URI, SIM_SMS_PROJECTION, null, null, DEFAULT_SORT_ORDER);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    final int rowIdIndex = cursor.getColumnIndex(_ID);
                    final int threadIdIndex = cursor.getColumnIndex(THREAD_ID);
                    final int addressIndex = cursor.getColumnIndex(ADDRESS);
                    final int dateIndex = cursor.getColumnIndex(DATE);
                    final int protocolIndex = cursor.getColumnIndex(PROTOCOL);
                    final int readIndex = cursor.getColumnIndex(READ);
                    final int statusIndex = cursor.getColumnIndex(STATUS);
                    final int typeIndex = cursor.getColumnIndex(TYPE);
                    final int bodyIndex = cursor.getColumnIndex(BODY);
                    final int serviceCenterIndex = cursor.getColumnIndex(SERVICE_CENTER);
                    final int lockedIndex = cursor.getColumnIndex(LOCKED);
                    final int errorCodeIndex = cursor.getColumnIndex(ERROR_CODE);

                    do {
                        Sms sms = new Sms();

                        sms.rowId = cursor.getLong(rowIdIndex);
                        sms.threadId = cursor.getLong(threadIdIndex);
                        sms.address = cursor.getString(addressIndex);
                        if (sms.person <= 0) {
                            sms.person = ContactUtil.getRawContactId(context, sms.address);
                        }
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
            } finally {
                cursor.close();
            }
        }

        return smsList;
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

        Cursor cursor = resolver.query(SMS_ALL_URI, PHONE_SMS_PROJECTION, null, null, DEFAULT_SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final int rowIdIndex = cursor.getColumnIndex(_ID);
                final int threadIdIndex = cursor.getColumnIndex(THREAD_ID);
                final int personIndex = cursor.getColumnIndex(PERSON);
                final int addressIndex = cursor.getColumnIndex(ADDRESS);
                final int dateIndex = cursor.getColumnIndex(DATE);
                final int protocolIndex = cursor.getColumnIndex(PROTOCOL);
                final int readIndex = cursor.getColumnIndex(READ);
                final int statusIndex = cursor.getColumnIndex(STATUS);
                final int typeIndex = cursor.getColumnIndex(TYPE);
                final int bodyIndex = cursor.getColumnIndex(BODY);
                final int serviceCenterIndex = cursor.getColumnIndex(SERVICE_CENTER);
                final int lockedIndex = cursor.getColumnIndex(LOCKED);
                final int errorCodeIndex = cursor.getColumnIndex(ERROR_CODE);

                do {
                    Sms sms = new Sms();

                    sms.rowId = cursor.getLong(rowIdIndex);
                    sms.threadId = cursor.getLong(threadIdIndex);
                    sms.address = cursor.getString(addressIndex);
                    sms.person = cursor.getLong(personIndex);
                    if (sms.person <= 0) {
                        sms.person = ContactUtil.getRawContactId(context, sms.address);
                    }
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
    public static boolean deletePhoneSms(Context context, long rowId) {
        return context.getContentResolver().delete(Uri.parse(SMS_ALL + "/" + rowId), null, null) > 0;
    }

    public static boolean deletePhoneSms(Context context, List<Long> rowIds) {
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

    public static boolean deletePhoneSmsAll(Context context) {
        return context.getContentResolver().delete(SMS_ALL_URI, null, null) > 0;
    }

    public static long sendSms(Context context, Sms sms) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(sms.address)) {
            Slog.e("address invalid: " + sms.address);
            return INVALID_ROW_ID;
        }

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messages = smsManager.divideMessage(sms.body);
        smsManager.sendMultipartTextMessage(sms.address, null, messages, null, null);

        ContentValues values = new ContentValues();
        values.put(ADDRESS, sms.address);
        /** one phoneNum only has one draft sms, discard exceed body **/
        values.put(BODY, sms.body);
        values.put(DATE, System.currentTimeMillis());
        Uri newSmsUri = context.getContentResolver().insert(SMS_SENT_URI, values);
        if (newSmsUri != null) {
            return ContentUris.parseId(newSmsUri);
        } else {
            return INVALID_ROW_ID;
        }
    }

    /**
     * @param context
     * @param sms
     * @return
     */
    public static long importPhoneSms(Context context, Sms sms) {

        ContentValues values = new ContentValues();
        values.put(ADDRESS, sms.address);
        /** one phoneNum only has one draft sms, discard exceed body **/
        values.put(BODY, sms.body);
        values.put(DATE, sms.date);
        Uri newSmsUri = context.getContentResolver().insert(typeToUri(sms.type), values);

        Slog.d("newSmsUri = " + newSmsUri);

        if (newSmsUri != null) {
            return ContentUris.parseId(newSmsUri);
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
