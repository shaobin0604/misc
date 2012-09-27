
package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class SetStatusRequest extends BaseMessage {
    private short mStatus;

    public SetStatusRequest(short status) {
        super();
        this.mMessageType = MSG_SET_STATUS_REQ;
        this.mStatus = status;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 2;
    }

}
