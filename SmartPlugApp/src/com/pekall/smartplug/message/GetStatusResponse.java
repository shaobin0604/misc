
package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class GetStatusResponse extends BaseMessage {
    private short mStatus;

    public GetStatusResponse(int messageId, short status) {
        super(messageId);
        this.mMessageType = MSG_GET_STATUS_RES;
        this.mStatus = status;
    }

    @Override
    public int size() {
        return super.size() + (Short.SIZE / Byte.SIZE); 
    }
    
    public short getStatus() {
        return mStatus;
    }

    @Override
    public String toString() {
        return "GetStatusResponse [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + ", mStatus="
                + mStatus + "]";
    }
    
    
}
