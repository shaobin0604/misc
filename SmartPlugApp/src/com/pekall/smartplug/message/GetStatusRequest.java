package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class GetStatusRequest extends BaseMessage {

    public GetStatusRequest(int messageId) {
        super(messageId);
        this.mMessageType = MSG_GET_STATUS_REQ; 
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public String toString() {
        return "GetStatusRequest [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + "]";
    }
}
