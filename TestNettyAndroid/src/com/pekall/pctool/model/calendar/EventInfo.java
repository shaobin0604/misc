
package com.pekall.pctool.model.calendar;

import android.provider.BaseColumns;

import com.pekall.pctool.Slog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.Adler32;

public class EventInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    transient public long id;
    transient public long calendarId;
    
    public String title;
    public String place;
    public long startTime;
    public long endTime;
    public int alertTime;
    public String rrule;
    public String note;
    public String timeZone;
    
    transient private long checksum;    // the cached checksum
    transient private boolean hasChecksum;
    transient public int modifyTag;     // event change flag(no change, add, update, delete) since last sync
    
    transient private static final Adler32 sChecksum = new Adler32();
    
    /**
     * Get checksum of this event info
     * 
     * @return checksum of this event info, 0 if error happens
     */
    public long getChecksum() {
        if (hasChecksum) {
            return checksum;
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            byte[] bytes = bos.toByteArray();
            synchronized (sChecksum) {
                sChecksum.reset();
                sChecksum.update(bytes);
                checksum = sChecksum.getValue();
                hasChecksum = true;
            }
        } catch (IOException e) {
            Slog.e("Error getChecksum", e);
            checksum = 0;
            hasChecksum = false;
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return checksum;
    }
    
    @Override
    public String toString() {
        return "EventInfo [evId=" + id + ", calendarId=" + calendarId + ", title=" + title + ", place=" + place
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
        result = prime * result + (int) (id ^ (id >>> 32));
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
        if (id != other.id)
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
        public static final String VERSION = "version";

        public long id;
        public long version;
        public int modifyTag;   // default same
        
        public EventVersion(long id, long version, int modifyTag) {
            super();
            this.id = id;
            this.version = version;
            this.modifyTag = modifyTag;
        }

        public EventVersion() {
            super();
            // TODO Auto-generated constructor stub
        }

        @Override
        public String toString() {
            return "EventVersion [id=" + id + ", version=" + version + ", modifyTag=" + modifyTag + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (id ^ (id >>> 32));
            result = prime * result + modifyTag;
            result = prime * result + (int) (version ^ (version >>> 32));
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
            EventVersion other = (EventVersion) obj;
            if (id != other.id)
                return false;
            if (modifyTag != other.modifyTag)
                return false;
            if (version != other.version)
                return false;
            return true;
        }
        
    }
}
