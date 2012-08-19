package com.pekall.pctool.model.mms;

import java.util.ArrayList;

public class Mms {
    public static final int MESSAGE_BOX_ALL    = 0;
    public static final int MESSAGE_BOX_INBOX  = 1;
    public static final int MESSAGE_BOX_SENT   = 2;
    public static final int MESSAGE_BOX_DRAFTS = 3;
    public static final int MESSAGE_BOX_OUTBOX = 4;
	
    long rowId;
    String phoneNum;
    String subject;
    long date;
    long isReaded;
    ArrayList<Slide> slides = new ArrayList<Slide>();
    ArrayList<Attachment> attachments = new ArrayList<Attachment>();

    public static class Slide {
        int duration;
        String text;
        byte[] image;
        byte[] audio;
        byte[] video;

        public Slide(int duration) {
            this.duration = duration;
        }
    }

    public static class Attachment {
        String name;
        byte[] fileBytes; // fileSize = fileBytes.length
    }
}
