package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class ReportStatusRequest extends BaseMessage {
    public short mStatus;   // 0 for off, 1 for on

    public ReportStatusRequest(short status) {
        super();
        this.mMessageType = MSG_REPORT_STATUS_REQ; 
        this.mStatus = status;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 2;
    }
    
    
}
