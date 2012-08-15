package com.pekall.pctool.model.sms;

public class Sms {
    
    /* @see frameworks/base/core/java/android/provider/Telephony.java */
    
    public static final int TYPE_ALL      = 0;
    public static final int TYPE_RECEIVED = 1;
    public static final int TYPE_SENT     = 2;
    public static final int TYPE_DRAFT    = 3;
    public static final int TYPE_OUTBOX   = 4;
    public static final int TYPE_FAILED   = 5; // for failed outgoing messages
    public static final int TYPE_QUEUED   = 6; // for messages to send later

    /* @see frameworks/base/core/java/android/provider/Telephony.java */
    
    /**
     * The TP-Status value for the message, or -1 if no status has
     * been received
     */
    public static final int STATUS_NONE = -1;
    public static final int STATUS_COMPLETE = 0;
    public static final int STATUS_PENDING = 32;
    public static final int STATUS_FAILED = 64;
    
    public static final int READ_TRUE = 1;
    public static final int READ_FALSE = 0;
    
    public long rowId;         // The id of Sms
    public String address;     // The phone number of the sender/recipient.
    public long date;          // The Java date representation (including millisecond) of the time when the message was sent/received. 
    public int protocol;       // Protocol used by the message, its mostly 0 in case of SMS messages.
    public int read;           // Read Message = 1, Unread Message = 0.
    public int status;         // None = -1, Complete = 0, Pending = 32, Failed = 64.
    public int type;           // 1 = Received, 2 = Sent, 3 = Draft, 4 = Outbox, 5 = Failed, 6 = Queued
    public String body;            // The content of the message.
    public String serviceCenter;   // The service center for the received message, null in case of sent messages.
    public int locked;
    public int errorCode;
    
    
    @Override
    public String toString() {
        return "Sms [rowId=" + rowId + ", address=" + address + ", date=" + date + ", protocol=" + protocol + ", read="
                + read + ", status=" + status + ", type=" + type + ", body=" + body + ", serviceCenter="
                + serviceCenter + ", locked=" + locked + ", errorCode=" + errorCode + "]";
    }
    
    
}
