package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.MSG_HEARTBEAT;

public class Heartbeat extends BaseMessage {
    private short mStatus;

    public Heartbeat(int messageId, short status) {
        super(messageId);
        this.mMessageType = MSG_HEARTBEAT;
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
        return "Heartbeat [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + ", mStatus=" + mStatus + "]";
    }
}
