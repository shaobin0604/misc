package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.model.DatabaseHelper;
import com.pekall.pctool.model.FastSyncUtils;
import com.pekall.pctool.model.calendar.CalendarUtil;
import com.pekall.pctool.model.calendar.EventInfo;
import com.pekall.pctool.model.calendar.EventInfo.EventVersion;
import com.pekall.pctool.util.Slog;

import java.util.Date;
import java.util.List;

public class EventChangesTestCase extends AndroidTestCase {
    
    private static final int DEFAULT_CALENDAR_ID = 1;

    private long mFirstEventId;
    private long mSecondEventId;
    private DatabaseHelper mDatabaseHelper;
    
    private void populateEvents() {
        
        EventInfo eventInfo = new EventInfo();
        
        eventInfo.alertTime = 100;
        eventInfo.title = "事件测试1";
        eventInfo.place = "华阳";
        eventInfo.note = "这是我的测试1";
        eventInfo.calendarId = DEFAULT_CALENDAR_ID;
        eventInfo.startTime = new Date().getTime() + 1000;
        eventInfo.endTime = eventInfo.startTime + 1000;
//        eventInfo.rrule = "FREQ=DAILY;WKST=SU";
        mFirstEventId = CalendarUtil.addEvent(mContext, eventInfo, false);
        
        assertTrue(mFirstEventId > 0);
        

        eventInfo = new EventInfo();
        
        eventInfo.alertTime = 200;
        eventInfo.title = "事件测试2";
        eventInfo.place = "华阳";
        eventInfo.note = "这是我的测试2";
        eventInfo.calendarId = DEFAULT_CALENDAR_ID;
        eventInfo.startTime = new Date().getTime() + 2000;
        eventInfo.endTime = eventInfo.startTime + 2000;
//        eventInfo.rrule = "FREQ=DAILY;WKST=SU";
        mSecondEventId = CalendarUtil.addEvent(mContext, eventInfo, false);
        
        assertTrue(mSecondEventId > 0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        CalendarUtil.deleteEventAll(getContext());
        
        populateEvents();

        mDatabaseHelper = new DatabaseHelper(getContext());
        mDatabaseHelper.open();

        List<EventVersion> eventVersions = CalendarUtil.getEventVersions(getContext());

        mDatabaseHelper.updateEventVersions(eventVersions);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        mDatabaseHelper.close();
        mDatabaseHelper = null;
    }
    
    public void testFindChanges() throws Exception {
        // add one event
        {
            EventInfo eventInfo = new EventInfo();
            
            eventInfo.alertTime = 200;
            eventInfo.title = "事件测试3";
            eventInfo.place = "华阳";
            eventInfo.note = "这是我的测试3";
            eventInfo.calendarId = DEFAULT_CALENDAR_ID;
            eventInfo.startTime = new Date().getTime() + 3000;
            eventInfo.endTime = eventInfo.startTime + 3000;
//            eventInfo.rrule = "FREQ=DAILY;WKST=SU";
            boolean success = CalendarUtil.addEvent(mContext, eventInfo, false) > 0;

            Slog.d("add event success = " + success);
        }
        // update one event
        {
            EventInfo eventInfo = CalendarUtil.queryEventById(getContext(), mFirstEventId);

            eventInfo.title = "事件测试1修改";
            
            boolean success = CalendarUtil.updateEvent(getContext(), eventInfo);
         
            Slog.d("update event success = " + success);
        }
        // delete one event
        {
            boolean success = CalendarUtil.deleteEvent(getContext(), mSecondEventId);
            Slog.d("delete event success = " + success);
        }
        

        List<EventVersion> eventVersions = FastSyncUtils.calculateEventChanges(
                CalendarUtil.getEventVersions(getContext()), mDatabaseHelper.getLastSyncEventVersions());

        Slog.d(eventVersions.toString());
    }
}
