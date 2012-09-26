package com.pekall.smartplug.message;

public enum MessageType {
    
    MSG_HELLO_REQ((short)100),
    MSG_HELLO_RES((short)101),
    MSG_REPORT_STATUS_REQ((short)102),
    MSG_REPORT_STATUS_RES((short)103),
    MSG_GET_STATUS_REQ((short)201),
    MSG_GET_STATUS_RES((short)202),
    MSG_SET_STATUS_REQ((short)203),
    MSG_SET_STATUS_RES((short)204);
    
    private short mValue;
    
    private MessageType(short value) {
        mValue = value;
    }
    
    public short getValue() {
        return mValue;
    }
}
