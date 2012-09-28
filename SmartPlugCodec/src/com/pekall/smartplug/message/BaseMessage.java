package com.pekall.smartplug.message;

public abstract class BaseMessage {
    protected MessageType mMessageType;
    protected int mMessageId;
    
    public BaseMessage(int messageId) {
        this.mMessageId = messageId;
    }
    
    public int size() {
        return Integer.SIZE / Byte.SIZE;
    }

    public MessageType getMessageType() {
        return mMessageType;
    }

    public int getMessageId() {
        return mMessageId;
    }
}
