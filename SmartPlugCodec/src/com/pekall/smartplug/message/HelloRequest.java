package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class HelloRequest extends BaseMessage {
    private String mDeviceId;        // max 32 bytes(including '\0')
    private String mDeviceMode;      // max 32 bytes(including '\0')
    
    public HelloRequest(int messageId, String deviceId, String deviceMode) {
        super(messageId);
        this.mMessageType = MSG_HELLO_REQ;
        this.mDeviceId = deviceId;
        this.mDeviceMode = deviceMode;
    }

    @Override
    public int size() {
        return super.size() + 32 + 32;
    }
    
    public String getDeviceId() {
        return mDeviceId;
    }
    
    public String getDeviceMode() {
        return mDeviceMode;
    }

    @Override
    public String toString() {
        return "HelloRequest [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + ", mDeviceId=" + mDeviceId
                + ", mDeviceMode=" + mDeviceMode + "]";
    }
}
