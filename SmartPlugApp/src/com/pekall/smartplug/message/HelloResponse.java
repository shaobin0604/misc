package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class HelloResponse extends BaseMessage {
    private short mResultCode;
    private String mServerName;
    
    public HelloResponse(int messageId, short resultCode, String serverName) {
        super(messageId);
        this.mMessageType = MSG_HELLO_RES;
        this.mResultCode = resultCode;
        this.mServerName = serverName;
    }
    
    public short getResultCode() {
        return mResultCode;
    }
    
    public String getServerName() {
        return mServerName;
    }

    @Override
    public int size() {
        return super.size() + (Short.SIZE / Byte.SIZE) + 32;
    }

    @Override
    public String toString() {
        return "HelloResponse [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + ", mResultCode="
                + mResultCode + ", mServerName=" + mServerName + "]";
    }
}
