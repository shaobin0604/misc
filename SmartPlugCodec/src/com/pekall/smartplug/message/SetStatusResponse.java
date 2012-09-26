
package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class SetStatusResponse extends BaseMessage {
    private short mResultCode;

    public SetStatusResponse(short resultCode) {
        super();
        this.mMessageType = MSG_SET_STATUS_RES;
        this.mResultCode = resultCode;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 2;
    }

}
