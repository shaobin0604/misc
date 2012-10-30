package com.pekall.pctool.model.mms;

import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;

public class Mms {
    public static final int MESSAGE_BOX_ALL    = 0;
    public static final int MESSAGE_BOX_INBOX  = 1;
    public static final int MESSAGE_BOX_SENT   = 2;
    public static final int MESSAGE_BOX_DRAFTS = 3;
    public static final int MESSAGE_BOX_OUTBOX = 4;
    
    public static final int READ_TRUE = 1;
    public static final int READ_FALSE = 0;
	
    public long rowId;
    public int msgBoxIndex; // 0: all, 1: inbox, 2: sent, 3: draft, 4: outbox
    public long threadId;
    public long person;
    public String phoneNum;
    public String subject;
    public long date;
    public int isReaded;
    public int size;
    public ArrayList<Slide> slides = new ArrayList<Slide>();
    public ArrayList<Attachment> attachments = new ArrayList<Attachment>();

    public static class Slide {
        public int duration;
        public String text;
        public int imageIndex = -1; // image's bytes = Mms.attachments.get(Mms.slides.get(0).imageIndex).fileBytes;
        public int audioIndex = -1;
        public int videoIndex = -1;

        public Slide(int duration) {
            this.duration = duration;
        }

        @Override
        public String toString() {
            return "Slide [duration=" + duration + ", text=" + "omit" + ", imageIndex=" + imageIndex + ", audioIndex="
                    + audioIndex + ", videoIndex=" + videoIndex + "]";
        }
        
        
    }

    public static class Attachment {
        public String name;
        public byte[] fileBytes; // fileSize = fileBytes.length
        
        @Override
        public String toString() {
            return "Attachment [name=" + name + ", fileBytes.length=" + fileBytes.length + "]";
        }
        
        
    }

    @Override
    public String toString() {
        return "Mms [rowId=" + rowId + ", size=" + size + ", msgBoxIndex=" + msgBoxIndex + ", phoneNum=" + phoneNum + ", subject="
                + subject + ", date=" + toDateStr(date) + ", isReaded=" + isReaded + ", slides=" + slides + ", attachments="
                + attachments + "]";
    }
    
    private static String toDateStr(long timeInMillis) {
        return DateFormat.format("MM/dd/yy h:mmaa", timeInMillis).toString();
    }
}
