package com.pekall.pctool.model.mms;

import java.util.ArrayList;

public class Mms {
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
