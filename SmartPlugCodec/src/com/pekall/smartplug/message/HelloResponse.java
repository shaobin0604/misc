package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class HelloResponse extends BaseMessage {
    public short mResultCode;
    public String mServerName;
    
    public HelloResponse(short resultCode, String serverName) {
        super();
        this.mMessageType = MSG_HELLO_RES;
        this.mResultCode = resultCode;
        this.mServerName = serverName;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 2 + 32;
    }
    
}
