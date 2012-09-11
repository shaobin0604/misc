
package com.pekall.pctool.model.calendar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.account.AccountInfo;
import com.pekall.pctool.model.calendar.EventInfo.EventVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CalendarUtil {

    private static final String DEFAULT_TIME_ZONE = TimeZone.getDefault().getID();

    public static final int INVALID_CALENDAR_ID = -1;

    /**
     * account
     */
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 2;
    private static final int PROJECTION_ACCOUNT_TYPE_INDEX = 3;
    private static final String CALENDAR_ACCOUNT_TYPE = "com.android.calendar.AccountType";
    private static final String CALENDAR_DEFAULT_NAME = "local calendar";

    /**
     * Events
     */
    private static final int PROJECTION_EVENTS_ID_INDEX = 0;
    private static final int PROJECTION_EVENTS_TITLE_INDEX = 1;
    private static final int PROJECTION_EVENTS_DESCRIPTION_INDEX = 2;
    private static final int PROJECTION_EVENTS_DTSTART_INDEX = 3;
    private static final int PROJECTION_EVENTS_DTEND_INDEX = 4;
    private static final int PROJECTION_EVENTS_RRULE_INDEX = 5;
    private static final int PROJECTION_EVENTS_LOCATION_INDEX = 6;
    private static final int PROJECTION_EVENTS_CALENDAR_ID_INDEX = 7;
    /**
     * Calendar
     */
    private static final int PROJECTION_CALENDAR_ID_INDEX = 0;
    private static final int PROJECTION_CALENDAR_NAME_INDEX = 1;
    /**
     * Reminder
     */
    private static final int PROJECTION_REMINDER_ID_INDEX = 0;
    private static final int PROJECTION_REMINDER_MINUTES_INDEX = 1;
    
    public static long getDefaultCalendarId(Context context) {
        List<CalendarInfo> calendarInfos = queryCalendarAll(context);
        if (calendarInfos.size() > 0) {
            // get the first calendar as default calendar for outlook sync 
            CalendarInfo calendarInfo = calendarInfos.get(0);
            return calendarInfo.caId;
        } else {
            // add a calendar as default calendar for outlook sync
            CalendarInfo calendarInfo = new CalendarInfo();
            calendarInfo.name = CALENDAR_DEFAULT_NAME;
            
            return addCalendar(context, calendarInfo);
        }
    }

    /**
     * 查询所有日历(所有账户的日历)
     */
    public static List<CalendarInfo> queryCalendarAll(Context context) {
        List<CalendarInfo> calendarInfoList = new ArrayList<CalendarInfo>();
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        cur = cr.query(uri, new String[] {
                Calendars._ID, Calendars.CALENDAR_DISPLAY_NAME, Calendars.ACCOUNT_NAME, Calendars.ACCOUNT_TYPE
        }, null, null, null);
        while (cur.moveToNext()) {
            CalendarInfo ci = new CalendarInfo();
            
            ci.caId = cur.getLong(PROJECTION_CALENDAR_ID_INDEX);
            ci.name = cur.getString(PROJECTION_CALENDAR_NAME_INDEX);
            ci.accountInfo.accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ci.accountInfo.accountType = cur.getString(PROJECTION_ACCOUNT_TYPE_INDEX);
            
            calendarInfoList.add(ci);
        }
        cur.close();
        return calendarInfoList;
    }

    /**
     * 查询某个账户下面的具体的日历
     */
    public static List<CalendarInfo> queryCalendarByAccount(Context context, AccountInfo account) {
        List<CalendarInfo> calendarInfoList = new ArrayList<CalendarInfo>();
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {
                account.accountName, account.accountType, account.accountName
        };
        cur = cr.query(uri, null, selection, selectionArgs, null);
        while (cur.moveToNext()) {
            long caId = cur.getLong(PROJECTION_CALENDAR_ID_INDEX);
            String name = cur.getString(PROJECTION_CALENDAR_NAME_INDEX);
            CalendarInfo ci = new CalendarInfo();
            ci.caId = caId;
            ci.name = name;
            ci.accountInfo = account;
            calendarInfoList.add(ci);
        }
        cur.close();
        return calendarInfoList;
    }

    /**
     * 修改一个日历
     */
    public static boolean updateCalendar(Context context, CalendarInfo cr) {
        if (cr == null)
            return false;
        long calID = cr.caId;
        ContentValues values = new ContentValues();
        // The new display name for the calendar
        values.put(Calendars.CALENDAR_DISPLAY_NAME, cr.name);
        Uri updateUri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calID);
        int rows = context.getContentResolver().update(updateUri, values, null, null);
        if (rows > 0)
            return true;
        return false;
    }

    /**
     * Add new Event
     * 
     * @param context
     * @param eventInfo
     * @return the new created Event id or -1 if error
     */
    public static long addEvent(Context context, EventInfo eventInfo) {
        Slog.d("addEvent E");

        // Slog.d("===== dump EventInfo =====");
        // Slog.d(eventInfo.toString());

        if (eventInfo.calendarId < 0) {
            Slog.e("Error calendarId: " + eventInfo.calendarId);
            return -1;
        }
        ContentValues eventValues = new ContentValues();
        eventValues.put(Events.TITLE, eventInfo.title);
        eventValues.put(Events.DESCRIPTION, eventInfo.note);
        eventValues.put(Events.EVENT_LOCATION, eventInfo.place);
        eventValues.put(Events.CALENDAR_ID, eventInfo.calendarId);
        eventValues.put(Events.EVENT_TIMEZONE, DEFAULT_TIME_ZONE);
        eventValues.put(Events.DTSTART, eventInfo.startTime);
        eventValues.put(Events.RRULE, eventInfo.rrule);

        // we caculate the duration should according to allday
        // if allday==1 use((er.endTime - er.startTime + DateUtils.DAY_IN_MILLIS
        // - 1) / DateUtils.DAY_IN_MILLIS)
        // if allday==1 timezone must use TIMEZONE_UTC

        if (!TextUtils.isEmpty(eventInfo.rrule)) {
            Slog.d("rrule is not empty");
            if (eventInfo.endTime - eventInfo.startTime > 0) {
                String duration = "P" + ((eventInfo.endTime - eventInfo.startTime) / DateUtils.SECOND_IN_MILLIS) + "S";
                eventValues.put(Events.DURATION, duration);
            } else {
                Slog.e("Error endTime must gt startTime");
            }
        } else {
            Slog.d("rrule is empty");
            eventValues.put(Events.DTEND, eventInfo.endTime);
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        // event
        ops.add(ContentProviderOperation
                .newInsert(Events.CONTENT_URI)
                .withValues(eventValues).build());

        // reminder
        ops.add(ContentProviderOperation
                .newInsert(Reminders.CONTENT_URI)
                .withValueBackReference(Reminders.EVENT_ID, 0)
                .withValue(Reminders.MINUTES, eventInfo.alertTime)
                .withValue(Reminders.METHOD, Reminders.METHOD_ALERT).build());

        ContentProviderResult[] results;
        try {
            results = context.getContentResolver().applyBatch(CalendarContract.AUTHORITY, ops);
            // insert Event result
            ContentProviderResult result = results[0];
            Slog.d("count = " + result.count + ", uri = " + result.uri);

            return ContentUris.parseId(result.uri);
        } catch (RemoteException e) {
            Slog.e("Error when addEvent", e);
            return -1;
        } catch (OperationApplicationException e) {
            Slog.e("Error when addEvent", e);
            return -1;
        }
    }

    /**
     * 修改一个事件 只提供部分可修改的属性 如果er.alertTime=0,则默认为没有修改提醒时间，则不必更新reminder表
     */
    public static boolean updateEvent(Context context, EventInfo eventInfo) {
        ContentValues eventValues = new ContentValues();

        eventValues.put(Events.TITLE, eventInfo.title);
        eventValues.put(Events.DESCRIPTION, eventInfo.note);
        eventValues.put(Events.EVENT_LOCATION, eventInfo.place);
        eventValues.put(Events.RRULE, eventInfo.rrule);
        eventValues.put(Events.DTSTART, eventInfo.startTime);
        if (!TextUtils.isEmpty(eventInfo.rrule)) {
            String duration = "P" + (eventInfo.endTime - eventInfo.startTime) + "S";
            eventValues.put(Events.DURATION, duration);
            eventValues.put(Events.DTEND, "");
        } else {
            eventValues.put(Events.DTEND, eventInfo.endTime);
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        // event
        ops.add(ContentProviderOperation
                .newUpdate(ContentUris.withAppendedId(Events.CONTENT_URI, eventInfo.id))
                .withValues(eventValues).build());

        // reminder
        if (eventInfo.alertTime != 0) {
            ops.add(ContentProviderOperation
                    .newUpdate(Reminders.CONTENT_URI)
                    .withValue(Reminders.MINUTES, eventInfo.alertTime)
                    .withSelection(Reminders.EVENT_ID + "=?", new String[] {String.valueOf(eventInfo.id)})
                    .build());
        }

        ContentProviderResult[] results;
        try {
            results = context.getContentResolver().applyBatch(CalendarContract.AUTHORITY, ops);
            return true;
        } catch (RemoteException e) {
            Slog.e("Error when updateEvent", e);
            return false;
        } catch (OperationApplicationException e) {
            Slog.e("Error when updateEvent", e);
            return false;
        }
    }

    public static int deleteEventAll(Context context) {
        long[] eventIds = queryEventIds(context);
        
        if (eventIds == null || eventIds.length == 0) {
            return 0;
        }
        
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        for (long id : eventIds) {
            ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Events.CONTENT_URI, id)).build());
        }
        
        ContentProviderResult[] results;
        try {
            results = context.getContentResolver().applyBatch(CalendarContract.AUTHORITY, ops);
            return results.length;
        } catch (RemoteException e) {
            Slog.e("Error when batch delete Event", e);
            return 0;
        } catch (OperationApplicationException e) {
            Slog.e("Error when batch delete Event", e);
            return 0;
        }
    }
    
    /**
     * 删除一个事件
     */
    public static boolean deleteEvent(Context context, EventInfo eventInfo) {
        return deleteEvent(context, eventInfo.id);
    }

    public static boolean deleteEvent(Context context, long eventInfoId) {
        ContentResolver cr = context.getContentResolver();
        Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventInfoId);
        int rows = cr.delete(deleteUri, null, null);
        if (rows > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 查询某个事件下面的提醒时间
     */
    public static int[] getReminderTime(Context context, EventInfo eventInfo) {
        return getReminderTime(context, eventInfo.id);
    }

    public static int[] getReminderTime(Context context, long eventId) {
        ContentResolver cr = context.getContentResolver();
        String selection = Reminders.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {
                String.valueOf(eventId)
        };
        Cursor cur = cr.query(Reminders.CONTENT_URI, new String[] {
                Reminders._ID, Reminders.MINUTES
        }, selection, selectionArgs, null);
        int[] alertTime = new int[cur.getCount()];
        int i = 0;
        while (cur.moveToNext()) {
            alertTime[i] = cur.getInt(PROJECTION_REMINDER_MINUTES_INDEX);
            i++;
        }
        cur.close();
        return alertTime;
    }

    public static List<EventVersion> getEventVersions(Context context) {
        List<EventInfo> eventInfos = queryEvents(context);

        List<EventVersion> eventVersions = new ArrayList<EventInfo.EventVersion>();

        for (EventInfo eventInfo : eventInfos) {
            EventVersion eventVersion = new EventVersion();

            eventVersion.id = eventInfo.id;
            eventVersion.version = eventInfo.getChecksum();

            eventVersions.add(eventVersion);
        }
        return eventVersions;
    }
    
    public static long[] queryEventIds(Context context) {
        String projection[] = new String[] {
                Events._ID, 
        };

        Cursor cursor = context.getContentResolver().query(Events.CONTENT_URI, projection, null, null, null);
        
        long[] ids = new long[0];
        if (cursor != null) {
            ids = new long[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()) {
                ids[i] = cursor.getLong(0);
                i++;
            }
            cursor.close();
        }
        return ids;
    }

    public static List<EventInfo> queryEvents(Context context) {
        return queryEventsByCalendarId(context, 0);
    }

    /**
     * Query events for a calendar, if the calendar is empty, return all events.
     * Note: synchronization when only set an account can query the account the
     * following event
     * 
     * @param cr
     * @param context
     * @return
     */
    public static List<EventInfo> queryEventsByCalendarId(Context context, long calendarId) {
        List<EventInfo> er = new ArrayList<EventInfo>();
        String projection[] = new String[] {
                Events._ID, Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.RRULE,
                Events.EVENT_LOCATION, Events.CALENDAR_ID,
        };

        String selection = null;
        String[] selectionArgs = null;

        if (calendarId > 0) {
            selection = Events.CALENDAR_ID + "=?";
            selectionArgs = new String[] {
                String.valueOf(calendarId)
            };
        }

        Cursor cur = context.getContentResolver().query(Events.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cur != null) {
            while (cur.moveToNext()) {
                EventInfo evr = new EventInfo();

                evr.id = cur.getLong(PROJECTION_EVENTS_ID_INDEX);
                evr.title = cur.getString(PROJECTION_EVENTS_TITLE_INDEX);
                evr.note = cur.getString(PROJECTION_EVENTS_DESCRIPTION_INDEX);
                evr.startTime = cur.getLong(PROJECTION_EVENTS_DTSTART_INDEX);
                evr.endTime = cur.getLong(PROJECTION_EVENTS_DTEND_INDEX);
                evr.rrule = cur.getString(PROJECTION_EVENTS_RRULE_INDEX);
                evr.place = cur.getString(PROJECTION_EVENTS_LOCATION_INDEX);
                evr.calendarId = cur.getLong(PROJECTION_EVENTS_CALENDAR_ID_INDEX);
                int[] reminds = getReminderTime(context, evr);
                if (reminds.length > 0) {
                    evr.alertTime = reminds[0];
                }

                er.add(evr);
            }
            cur.close();
        }
        return er;
    }

    public static long queryEventVersion(Context context, long id) {
        EventInfo eventInfo = queryEventById(context, id);
        return eventInfo.getChecksum();
    }

    /**
     * 查询到某一个事件
     */
    public static EventInfo queryEventById(Context context, long id) {
        EventInfo evr = null;
        String projection[] = new String[] {
                Events._ID, Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.RRULE,
                Events.EVENT_LOCATION
        };
        String selection = Events._ID + "=?";
        String selectionArgs[] = {
                String.valueOf(id)
        };
        Cursor cursor = context.getContentResolver().query(Events.CONTENT_URI, projection, selection, selectionArgs,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                evr = new EventInfo();

                evr.id = cursor.getLong(PROJECTION_EVENTS_ID_INDEX);
                evr.title = cursor.getString(PROJECTION_EVENTS_TITLE_INDEX);
                evr.note = cursor.getString(PROJECTION_EVENTS_DESCRIPTION_INDEX);
                evr.startTime = cursor.getLong(PROJECTION_EVENTS_DTSTART_INDEX);
                evr.endTime = cursor.getLong(PROJECTION_EVENTS_DTEND_INDEX);
                evr.rrule = cursor.getString(PROJECTION_EVENTS_RRULE_INDEX);
                evr.place = cursor.getString(PROJECTION_EVENTS_LOCATION_INDEX);
                int[] reminds = getReminderTime(context, evr);
                if (reminds.length > 0) {
                    evr.alertTime = reminds[0];
                }
            }
            cursor.close();
        }
        return evr;
    }

    /**
     * delete a calendar then will delete all events
     */
    public static boolean deleteCalendar(Context context, long calendarId) {
        Uri uri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calendarId);
        int rows = context.getContentResolver().delete(uri, null, null);
        if (rows > 0)
            return true;
        return false;
    }

    /**
     * add a calendar if has no calendars add a calendar and add a account These
     * fields are only writable by a sync adapter. To modify them the caller
     * must include CALLER_IS_SYNCADAPTER
     * 
     * @param context
     * @param calendarInfo
     * @return
     */
    public static long addCalendar(Context context, CalendarInfo calendarInfo) {
        ContentValues values = new ContentValues();
        AccountManager accountManager = (AccountManager) context
                .getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccounts();
        String name = CALENDAR_DEFAULT_NAME;
        String type = CALENDAR_ACCOUNT_TYPE;
        // if have accounts
        if (accounts != null && accounts.length > 0) {
            name = accounts[0].name;
            type = accounts[0].type;
        }
        values.put(Calendars.ACCOUNT_NAME, name);
        values.put(Calendars.ACCOUNT_TYPE, type);
        values.put(Calendars._SYNC_ID, "0");
        Time time = new Time("UTC");
        time.setToNow();
        values.put(Calendars.CAL_SYNC1, "LOCAL://0.0");
        values.put(Calendars.DIRTY, 0);
        values.put(Calendars.NAME, calendarInfo.name);
        values.put(Calendars.CALENDAR_DISPLAY_NAME, calendarInfo.name);
        values.put(Calendars.CALENDAR_COLOR, -14069085);
        values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
        values.put(Calendars.CALENDAR_LOCATION, "location");
        values.put(Calendars.CALENDAR_TIME_ZONE, DEFAULT_TIME_ZONE);
        values.put(Calendars.OWNER_ACCOUNT, calendarInfo.name);
        final Uri calendarUri = Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, name)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, type)
                .build();
        
        Slog.d("calendarUri = " + calendarUri);
        
        Uri newCalendarUri = context.getContentResolver().insert(calendarUri, values);
        
        Slog.d("newCalendarUri = " + newCalendarUri);
        
        return ContentUris.parseId(newCalendarUri);
    }

}
