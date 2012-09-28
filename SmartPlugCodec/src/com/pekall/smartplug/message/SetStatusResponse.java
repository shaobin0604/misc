
package com.pekall.smartplug.message;

import static com.pekall.smartplug.message.MessageType.*;

public class SetStatusResponse extends BaseMessage {
    private short mResultCode;

    public SetStatusResponse(int messageId, short resultCode) {
        super(messageId);
        this.mMessageType = MSG_SET_STATUS_RES;
        this.mResultCode = resultCode;
    }
    
    public short getResultCode() {
        return mResultCode;
    }

    @Override
    public int size() {
        return super.size() + (Short.SIZE / Byte.SIZE);
    }

    @Override
    public String toString() {
        return "SetStatusResponse [mMessageType=" + mMessageType + ", mMessageId=" + mMessageId + ", mResultCode="
                + mResultCode + "]";
    }

}
