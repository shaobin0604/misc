
package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.calendar.CalendarInfo;
import com.pekall.pctool.model.calendar.CalendarUtil;
import com.pekall.pctool.model.calendar.EventInfo;

import junit.framework.Assert;

import java.util.Date;
import java.util.List;


public class CalendarUtilTestCase extends AndroidTestCase {

    public void testAddCalendar() throws Exception {
        CalendarInfo calendarInfo = new CalendarInfo();

        calendarInfo.name = "test add calendar 2";

        long calendarId = CalendarUtil.addCalendar(getContext(), calendarInfo);

        assertTrue(calendarId > 0);
    }

    public void testAddEvent() throws Exception {
        EventInfo er = new EventInfo();
        er.alertTime = 100;
        er.note = "这是我的测试1";
        er.place = "华阳";
        er.title = "事件测试";
        er.calendarId = 1;
        er.startTime = new Date().getTime();
        er.endTime = er.startTime + 30 * 60 * 1000;
        boolean success = CalendarUtil.addEvent(mContext, er, false) > 0;
        System.out.println("------>flag" + success);
        Assert.assertTrue(success);
    }
    
    public void testQueryEvents() throws Exception {
        List<EventInfo> eventInfos = CalendarUtil.queryAllEvents(getContext());
        for (EventInfo eventInfo : eventInfos) {
            Slog.d(eventInfo.toString());
        }
    }
    
    public void testQueryAllCalendars() throws Exception {
        List<CalendarInfo> calendars = CalendarUtil.queryAllCalendars(getContext());
        Slog.d(calendars.toString());
    }
    
    public void testQueryPhoneCalendars() throws Exception {
        List<CalendarInfo> calendars = CalendarUtil.queryPhoneCalendars(getContext());
        Slog.d(calendars.toString());
    }
}
