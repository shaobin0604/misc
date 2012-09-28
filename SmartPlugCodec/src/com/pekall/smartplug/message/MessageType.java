package com.pekall.smartplug.message;

public enum MessageType {
    
    MSG_HELLO_REQ((short)100),
    MSG_HELLO_RES((short)101),
    MSG_REPORT_STATUS_REQ((short)102),
    MSG_REPORT_STATUS_RES((short)103),
    MSG_HEARTBEAT((short)104),
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
    
    public static MessageType fromValue(short value) {
        switch (value) {
            case 100:
                return MSG_HELLO_REQ;
            case 101:
                return MSG_HELLO_RES;
            case 102:
                return MSG_REPORT_STATUS_REQ;
            case 103:
                return MSG_REPORT_STATUS_RES;
            case 104:
                return MSG_HEARTBEAT;
            case 201:
                return MSG_GET_STATUS_REQ;
            case 202:
                return MSG_GET_STATUS_RES;
            case 203:
                return MSG_SET_STATUS_REQ;
            case 204:
                return MSG_SET_STATUS_RES;
            default:
                throw new IllegalArgumentException("Unknown MessageType value: " + value);
        }
    }
}
