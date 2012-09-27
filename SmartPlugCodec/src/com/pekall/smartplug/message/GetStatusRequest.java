package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class GetStatusRequest extends BaseMessage {

    public GetStatusRequest() {
        super();
        this.mMessageType = MSG_GET_STATUS_REQ; 
    }

    @Override
    public int size() {
        return 0;
    }
}
