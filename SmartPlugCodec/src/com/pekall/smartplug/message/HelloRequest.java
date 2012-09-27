package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class HelloRequest extends BaseMessage {
    public String mDeviceId;        // max 32 bytes(including '\0')
    public String mDeviceMode;      // max 32 bytes(including '\0')
    
    public HelloRequest(String deviceId, String deviceMode) {
        super();
        this.mMessageType = MSG_HELLO_REQ;
        this.mDeviceId = deviceId;
        this.mDeviceMode = deviceMode;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 32 + 32;
    }
}
