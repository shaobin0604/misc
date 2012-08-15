
package com.pekall.pctool.model.calendar;

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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CalendarUtil {

    private static final String TIME_ZONE = TimeZone.getDefault().toString();
    
    /**
     * account
     */
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 2;
    private static final int PROJECTION_ACCOUNT_TYPE_INDEX = 3;
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
            ci.accountInfo = new AccountInfo();
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
            return false;
        }
        ContentValues event = new ContentValues();
        event.put(Events.TITLE, eventInfo.title);
        event.put(Events.DTSTART, eventInfo.startTime);
        event.put(Events.DESCRIPTION, eventInfo.note);
        event.put(Events.CALENDAR_ID, eventInfo.calendarId);
        event.put(Events.EVENT_TIMEZONE, TIME_ZONE);
        event.put(Events.RRULE, eventInfo.rrule);
        if (eventInfo.rrule != null || !"".equals(eventInfo.rrule)) {
            String duration = "P" + (eventInfo.endTime - eventInfo.startTime) + "S";
            event.put(Events.DURATION, duration);
        } else {
            event.put(Events.DTEND, eventInfo.endTime);
        }
        Uri newEvent = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, event);
        long id = Long.valueOf(newEvent.getLastPathSegment());
        if (id == 0) {
            return false;
        } else {
            ContentValues values = new ContentValues();
            values.put(Reminders.EVENT_ID, id);
            values.put(Reminders.MINUTES, eventInfo.alertTime);
            values.put(Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, values);
        }
        return true;
    }

    /**
     * 修改一个事件 只提供部分可修改的属性 如果er.alertTime=0,则默认为没有修改提醒时间，则不必更新reminder表
     */
    public static boolean updateEvent(Context context, EventInfo eventInfo) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.TITLE, eventInfo.title);
        values.put(Events.DESCRIPTION, eventInfo.note);
        values.put(Events.RRULE, eventInfo.rrule);
        values.put(Events.DTSTART, eventInfo.startTime);
        if (eventInfo.rrule != null || !"".equals(eventInfo.rrule)) {
            String duration = "P" + (eventInfo.endTime - eventInfo.startTime) + "S";
            values.put(Events.DURATION, duration);
            values.put(Events.DTEND, "");
        } else {
            values.put(Events.DTEND, eventInfo.endTime);
        }
        Uri myUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventInfo.evId);
        int rows = cr.update(myUri, values, null, null);
        if (rows == 0)
            return false;
        if (eventInfo.alertTime != 0) {
            values = new ContentValues();
            values.put(Reminders.MINUTES, eventInfo.alertTime);
            rows = cr.update(Reminders.CONTENT_URI, values, Reminders.EVENT_ID + "=?", new String[] {
                    String.valueOf(eventInfo.evId)
            });
            if (rows == 0)
                return false;
        }
        return true;
    }

    /**
     * 删除一个事件
     */
    public static boolean deleteEvent(Context context, EventInfo eventInfo) {
        ContentResolver cr = context.getContentResolver();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventInfo.evId);
        int rows = cr.delete(deleteUri, null, null);
        if (rows > 0)
            return true;
        return false;
    }

    /**
     * 查询某个事件下面的提醒时间
     */
    public static int[] getReminderTime(Context context, EventInfo eventInfo) {
        ContentResolver cr = context.getContentResolver();
        String selection = Reminders.EVENT_ID + " = ? ";
        String[] selectionArgs = new String[] {
                String.valueOf(eventInfo.evId)
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
    public static List<EventInfo> getEvents(Context context, CalendarInfo cr) {
        List<EventInfo> er = new ArrayList<EventInfo>();
        Cursor cur = null;
        Uri uri = Events.CONTENT_URI;
        String projection[] = new String[] {
                Events._ID, Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.RRULE,
                Events.EVENT_LOCATION
        };
        if (null == cr) {
            cur = context.getContentResolver().query(uri, projection, null, null, null);
        } else {
            String selection = Events.CALENDAR_ID + "=?";
            String selectionArgs[] = {
                    String.valueOf(cr.caId)
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
            evr.calendarId = cr.caId;

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
     * add calendar not work it needs sync_adapter
     * 
     * @param context
     */
    public static void createNewCalendar(Context context) {
        ContentValues calendar = new ContentValues();
        calendar.put("_sync_account", sync_account); // My account
        calendar.put("_sync_account_type", "com.google");
        // calendar.put("_sync_id", 1); // null
        calendar.put("name", "ccc");
        calendar.put("displayName", "xxxxxx");
        calendar.put("hidden", 0);
        calendar.put("color", 0xFF008080);
        calendar.put("access_level", 700);
        // calendar.put("selected", 0); // 0
        calendar.put("sync_events", 1);
        calendar.put("timezone", "Europe/Paris");
        calendar.put("ownerAccount", sync_account);
        Uri calendarUri = Calendars.CONTENT_URI;
        context.getContentResolver().insert(calendarUri, calendar);
    }

    /**
     * delete a calendar
     */
    public static boolean deleteCalendar(Context context, long calendarId) {
        Uri uri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calendarId);
        int rows = context.getContentResolver().delete(uri, null, null);
        if (rows > 0)
            return true;
        return false;

    }
}
