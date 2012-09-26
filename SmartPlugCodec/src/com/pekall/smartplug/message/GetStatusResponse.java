
package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class GetStatusResponse extends BaseMessage {
    private short mStatus;

    public GetStatusResponse(short status) {
        super();
        this.mMessageType = MSG_GET_STATUS_RES;
        this.mStatus = status;
    }

    @Override
    public int size() {
        return 2;
    }
}
