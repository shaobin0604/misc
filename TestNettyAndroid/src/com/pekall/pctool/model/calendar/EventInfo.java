
package com.pekall.pctool.model.calendar;

import android.provider.BaseColumns;

public class EventInfo {
    public long evId;
    public long calendarId;
    public String title;
    public String place;
    public long startTime;
    public long endTime;
    public int alertTime;
    public String rrule;
    public String note;
    public String timeZone;
    
    @Override
    public String toString() {
        return "EventInfo [evId=" + evId + ", calendarId=" + calendarId + ", title=" + title + ", place=" + place
                + ", startTime=" + startTime + ", endTime=" + endTime + ", alertTime=" + alertTime + ", rrule=" + rrule
                + ", note=" + note + ", timeZone=" + timeZone + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + alertTime;
        result = prime * result + (int) (calendarId ^ (calendarId >>> 32));
        result = prime * result + (int) (endTime ^ (endTime >>> 32));
        result = prime * result + (int) (evId ^ (evId >>> 32));
        result = prime * result + ((note == null) ? 0 : note.hashCode());
        result = prime * result + ((place == null) ? 0 : place.hashCode());
        result = prime * result + ((rrule == null) ? 0 : rrule.hashCode());
        result = prime * result + (int) (startTime ^ (startTime >>> 32));
        result = prime * result + ((timeZone == null) ? 0 : timeZone.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventInfo other = (EventInfo) obj;
        if (alertTime != other.alertTime)
            return false;
        if (calendarId != other.calendarId)
            return false;
        if (endTime != other.endTime)
            return false;
        if (evId != other.evId)
            return false;
        if (note == null) {
            if (other.note != null)
                return false;
        } else if (!note.equals(other.note))
            return false;
        if (place == null) {
            if (other.place != null)
                return false;
        } else if (!place.equals(other.place))
            return false;
        if (rrule == null) {
            if (other.rrule != null)
                return false;
        } else if (!rrule.equals(other.rrule))
            return false;
        if (startTime != other.startTime)
            return false;
        if (timeZone == null) {
            if (other.timeZone != null)
                return false;
        } else if (!timeZone.equals(other.timeZone))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }
    
    public static class EventVersion implements BaseColumns {
        public long id;
        
    }
}
