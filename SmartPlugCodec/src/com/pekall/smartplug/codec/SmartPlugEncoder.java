package com.pekall.smartplug.codec;

import com.pekall.smartplug.message.BaseMessage;
import com.pekall.smartplug.message.HelloRequest;
import com.pekall.smartplug.message.HelloResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SmartPlugEncoder extends OneToOneEncoder {
    private static final int HEADER_SIZE = 4;   // msg_type: short, msg_length: short
    private static final int STRING_MAX_BYTES = 31;

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof BaseMessage) {
            BaseMessage baseMessage = (BaseMessage) msg;
            switch (baseMessage.mMessageType) {
                case MSG_HELLO_REQ:
                    return encodeHelloRequest((HelloRequest) baseMessage);
                case MSG_HELLO_RES:
                    return encodeHelloResponse((HelloResponse) baseMessage);
                default:
                    throw new IllegalArgumentException("unknown MessageType");
            }
        } else {
            throw new IllegalArgumentException("not BaseMessage instance");
        }
    }
    
    private Object encodeHelloRequest(HelloRequest request) throws UnsupportedEncodingException {
        ChannelBuffer buffer = ChannelBuffers.buffer(getChannelBufferSize(request));
        writeHeader(buffer, request);
        writeStringUTF(buffer, request.mDeviceId);  // deviceId
        writeStringUTF(buffer, request.mDeviceMode);// deviceMode
        return buffer;
    }

    private Object encodeHelloResponse(HelloResponse response) throws UnsupportedEncodingException {
    	ChannelBuffer buffer = ChannelBuffers.buffer(getChannelBufferSize(response));
    	writeHeader(buffer, response);
    	buffer.writeShort(response.mResultCode);
    	writeStringUTF(buffer, response.mServerName);
        return null;
    }

    private static void writeHeader(ChannelBuffer buffer, BaseMessage baseMessage) {
		buffer.writeShort(baseMessage.mMessageType.getValue());  // type
        buffer.writeShort(baseMessage.size());                   // length
	}
    
    private static int getChannelBufferSize(BaseMessage message) {
        return HEADER_SIZE + message.size();
    }
    
    private static void writeStringUTF(ChannelBuffer buffer, String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("utf-8");
        
        int writeLength = Math.min(bytes.length, STRING_MAX_BYTES);
        buffer.writeBytes(bytes, 0, writeLength);
        
        int fillCount = (STRING_MAX_BYTES + 1) - writeLength;
        
        for (int i = 0; i < fillCount; i++) {
            buffer.writeByte(0);
        }
    }
}
