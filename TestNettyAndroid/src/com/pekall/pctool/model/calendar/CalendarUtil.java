
package com.pekall.pctool.model.calendar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.text.TextUtils;
import android.text.format.Time;

import com.pekall.pctool.R;
import com.pekall.pctool.Slog;
import com.pekall.pctool.model.account.AccountInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CalendarUtil {

    private static final String TIME_ZONE = "Asia/Shanghai";

    public static final int INVALID_CALENDAR_ID = -1;

    /**
     * account
     */
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 2;
    private static final int PROJECTION_ACCOUNT_TYPE_INDEX = 3;
    private static final String CALENDAR_ACCOUNT_NAME = "LocalAccount";
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

    private static final String CALENDAR_NAME = "mycalendar";
    private static final String sync_account = "fanqinglintiantian@gmail.com";

    /**
     * 查询所有日历(所有账户的日历)
     */
    public static List<CalendarInfo> getAllCalendarInfos(Context context) {
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
    public static List<CalendarInfo> getAllCalendarOfAccount(Context context, AccountInfo account) {
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
     * addEvent must need calendarId
     * 
     * @param context
     * @param EventInfo
     */
    public static boolean addEvent(Context context, EventInfo eventInfo) {
        if (eventInfo.calendarId < 0) {
            Slog.e("Error calendarId: " + eventInfo.calendarId);
            return false;
        }
        ContentValues event = new ContentValues();
        event.put(Events.TITLE, eventInfo.title);
        event.put(Events.DESCRIPTION, eventInfo.note);
        event.put(Events.EVENT_LOCATION, eventInfo.place);
        event.put(Events.CALENDAR_ID, eventInfo.calendarId);
        event.put(Events.EVENT_TIMEZONE, TIME_ZONE);
        event.put(Events.DTSTART, eventInfo.startTime);
        event.put(Events.RRULE, eventInfo.rrule);
        if (!TextUtils.isEmpty(eventInfo.rrule)) {
            String duration = "P" + (eventInfo.endTime - eventInfo.startTime) + "S";
            event.put(Events.DURATION, duration);
        } else {
            event.put(Events.DTEND, eventInfo.endTime);
        }
        Uri newEvent = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, event);
        long id = Long.valueOf(newEvent.getLastPathSegment());
        if (id == 0) {
            Slog.e("Error cannot insert newEvent");
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(Reminders.EVENT_ID, id);
            values.put(Reminders.MINUTES, eventInfo.alertTime);
            values.put(Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri newReminder = context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, values);
            Slog.d("newReminder: " + newReminder);
            return newReminder != null;
        }
    }

    /**
     * 修改一个事件 只提供部分可修改的属性 如果er.alertTime=0,则默认为没有修改提醒时间，则不必更新reminder表
     */
    public static boolean updateEvent(Context context, EventInfo eventInfo) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.TITLE, eventInfo.title);
        values.put(Events.DESCRIPTION, eventInfo.note);
        values.put(Events.EVENT_LOCATION, eventInfo.place);
        values.put(Events.RRULE, eventInfo.rrule);
        values.put(Events.DTSTART, eventInfo.startTime);
        if (!TextUtils.isEmpty(eventInfo.rrule)) {
            String duration = "P" + (eventInfo.endTime - eventInfo.startTime) + "S";
            values.put(Events.DURATION, duration);
            values.put(Events.DTEND, "");
        } else {
            values.put(Events.DTEND, eventInfo.endTime);
        }
        Uri myUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventInfo.evId);
        int rows = cr.update(myUri, values, null, null);
        if (rows == 0) {
            Slog.e("Error update Event");
            return false;
        }
        if (eventInfo.alertTime != 0) {
            values = new ContentValues();
            values.put(Reminders.MINUTES, eventInfo.alertTime);
            rows = cr.update(Reminders.CONTENT_URI, values, Reminders.EVENT_ID + "=?", new String[] {
                    String.valueOf(eventInfo.evId)
            });
            if (rows == 0) {
                Slog.e("Error update reminder");
                return false;
            }
        }
        return true;
    }

    /**
     * 删除一个事件
     */
    public static boolean deleteEvent(Context context, EventInfo eventInfo) {
        return deleteEvent(context, eventInfo.evId);
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
        return getReminderTime(context, eventInfo.evId);
    }

    public static int[] getReminderTime(Context context, long eventInfoId) {
        ContentResolver cr = context.getContentResolver();
        String selection = Reminders.EVENT_ID + " = ? ";
        String[] selectionArgs = new String[] {
                String.valueOf(eventInfoId)
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
        return alertTime;
    }

    /**
     * 查询事件 针对某个日历，若日历为空，则返回所有的事件 注意：当只有设置某账户同步的时候，才能查询到该账户下面的事件
     * 
     * @param context
     * @param cr
     * @return
     */
    public static List<EventInfo> getEvents(Context context, long calendarId) {
        List<EventInfo> er = new ArrayList<EventInfo>();
        Cursor cur = null;
        Uri uri = Events.CONTENT_URI;
        String projection[] = new String[] {
                Events._ID, Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.RRULE,
                Events.EVENT_LOCATION
        };
        if (calendarId <= 0) {
            cur = context.getContentResolver().query(uri, projection, null, null, null);
        } else {
            String selection = Events.CALENDAR_ID + "=?";
            String selectionArgs[] = {
                    String.valueOf(calendarId)
            };
            cur = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        }
        while (cur.moveToNext()) {
            EventInfo evr = new EventInfo();

            evr.evId = cur.getLong(PROJECTION_EVENTS_ID_INDEX);
            evr.title = cur.getString(PROJECTION_EVENTS_TITLE_INDEX);
            evr.note = cur.getString(PROJECTION_EVENTS_DESCRIPTION_INDEX);
            evr.startTime = cur.getLong(PROJECTION_EVENTS_DTSTART_INDEX);
            evr.endTime = cur.getLong(PROJECTION_EVENTS_DTEND_INDEX);
            evr.rrule = cur.getString(PROJECTION_EVENTS_RRULE_INDEX);
            evr.place = cur.getString(PROJECTION_EVENTS_LOCATION_INDEX);
            int[] reminds = getReminderTime(context, evr);
            if (reminds.length > 0) {
                evr.alertTime = reminds[0];
            }
            evr.calendarId = calendarId;

            er.add(evr);
        }
        return er;
    }

    /**
     * 查询到某一个事件
     */
    public static EventInfo queryByEventId(Context context, long evId) {
        Uri uri = Events.CONTENT_URI;
        EventInfo evr = null;
        String projection[] = new String[] {
                Events._ID, Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.RRULE,
                Events.EVENT_LOCATION
        };
        String selection = Events._ID + "=?";
        String selectionArgs[] = {
                String.valueOf(evId)
        };
        Cursor cur = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        while (cur.moveToNext()) {
            evr = new EventInfo();
            evr.evId = cur.getLong(PROJECTION_EVENTS_ID_INDEX);
            evr.title = cur.getString(PROJECTION_EVENTS_TITLE_INDEX);
            evr.note = cur.getString(PROJECTION_EVENTS_DESCRIPTION_INDEX);
            evr.startTime = cur.getLong(PROJECTION_EVENTS_DTSTART_INDEX);
            evr.endTime = cur.getLong(PROJECTION_EVENTS_DTEND_INDEX);
            evr.rrule = cur.getString(PROJECTION_EVENTS_RRULE_INDEX);
            evr.place = cur.getString(PROJECTION_EVENTS_LOCATION_INDEX);
            int[] reminds = getReminderTime(context, evr);
            if (reminds.length > 0) {
                evr.alertTime = reminds[0];
            }
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
     * @param calendarRecord
     * @return
     */
    public static Uri addCalendar(Context context, CalendarInfo calendarInfo) {
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
        values.put(Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());
        values.put(Calendars.OWNER_ACCOUNT, calendarInfo.name);
        final Uri calendarUri = Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, name)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, type)
                .build();
        return context.getContentResolver().insert(calendarUri, values);
    }

}
