
package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class SetStatusRequest extends BaseMessage {
    private short mStatus;

    public SetStatusRequest(int messageId, short status) {
        super(messageId);
        this.mMessageType = MSG_SET_STATUS_REQ;
        this.mStatus = status;
    }
    
    public short getStatus() {
        return mStatus;
    }

    @Override
    public int size() {
        return super.size() + (Short.SIZE / Byte.SIZE);
    }

    @Override
    public String toString() {
        return "SetStatusRequest [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + ", mStatus=" + mStatus
                + "]";
    }

}
