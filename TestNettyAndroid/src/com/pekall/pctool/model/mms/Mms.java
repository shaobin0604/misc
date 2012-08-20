package com.pekall.pctool.model.mms;

import java.util.ArrayList;

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
    public String phoneNum;
    public String subject;
    public long date;
    public int isReaded;
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
    }

    public static class Attachment {
        public String name;
        public byte[] fileBytes; // fileSize = fileBytes.length
    }
}
