
package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class ReportStatusResponse extends BaseMessage {
    public ReportStatusResponse(int messageId) {
        super(messageId);
        this.mMessageType = MSG_REPORT_STATUS_RES;
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public String toString() {
        return "ReportStatusResponse [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + "]";
    }

    
}
