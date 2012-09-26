
package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class ReportStatusResponse extends BaseMessage {
    public ReportStatusResponse() {
        super();
        this.mMessageType = MSG_REPORT_STATUS_RES;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

}
