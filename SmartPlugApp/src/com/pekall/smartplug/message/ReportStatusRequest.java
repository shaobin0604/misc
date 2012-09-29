package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class ReportStatusRequest extends BaseMessage {
    private short mStatus;   // 0 for off, 1 for on

    public ReportStatusRequest(int messageId, short status) {
        super(messageId);
        this.mMessageType = MSG_REPORT_STATUS_REQ; 
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
        return "ReportStatusRequest [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + ", mStatus="
                + mStatus + "]";
    }
    
    
}
