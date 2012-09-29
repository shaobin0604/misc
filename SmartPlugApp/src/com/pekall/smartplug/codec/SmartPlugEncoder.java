package com.pekall.smartplug.codec;

import com.pekall.smartplug.message.BaseMessage;
import com.pekall.smartplug.message.GetStatusRequest;
import com.pekall.smartplug.message.GetStatusResponse;
import com.pekall.smartplug.message.Heartbeat;
import com.pekall.smartplug.message.HelloRequest;
import com.pekall.smartplug.message.HelloResponse;
import com.pekall.smartplug.message.ReportStatusRequest;
import com.pekall.smartplug.message.ReportStatusResponse;
import com.pekall.smartplug.message.SetStatusRequest;
import com.pekall.smartplug.message.SetStatusResponse;

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
            switch (baseMessage.getMessageType()) {
                case MSG_HELLO_REQ:
                    return encodeHelloRequest((HelloRequest) baseMessage);
                case MSG_HELLO_RES:
                    return encodeHelloResponse((HelloResponse) baseMessage);
                case MSG_REPORT_STATUS_REQ:
                    return encodeReportStatusRequest((ReportStatusRequest) baseMessage);
                case MSG_REPORT_STATUS_RES:
                    return encodeReportStatusResponse((ReportStatusResponse) baseMessage);
                case MSG_HEARTBEAT:
                    return encodeHeartbeat((Heartbeat) baseMessage);
                case MSG_GET_STATUS_REQ:
                    return encodeGetStatusRequest((GetStatusRequest) baseMessage);
                case MSG_GET_STATUS_RES:
                    return encodeGetStatusResponse((GetStatusResponse) baseMessage);
                case MSG_SET_STATUS_REQ:
                    return encodeSetStatusRequest((SetStatusRequest) baseMessage);
                case MSG_SET_STATUS_RES:
                    return encodeSetStatusResponse((SetStatusResponse) baseMessage);
                default:
                    throw new IllegalArgumentException("unknown MessageType");
            }
        } else {
            throw new IllegalArgumentException("not BaseMessage instance");
        }
    }


    private Object encodeHeartbeat(Heartbeat request) {
        ChannelBuffer buffer = prepareBuffer(request);
        
        buffer.writeShort(request.getStatus());
        
        return buffer;
    }


    private Object encodeSetStatusRequest(SetStatusRequest request) {
        ChannelBuffer buffer = prepareBuffer(request);
        
        buffer.writeShort(request.getStatus());
        
        return buffer;
    }


    private Object encodeSetStatusResponse(SetStatusResponse response) {
        ChannelBuffer buffer = prepareBuffer(response);
        
        buffer.writeShort(response.getResultCode());
        
        return buffer;
    }


    private Object encodeGetStatusRequest(GetStatusRequest request) {
        ChannelBuffer buffer = prepareBuffer(request);
        
        return buffer;
    }

    private Object encodeGetStatusResponse(GetStatusResponse response) {
        ChannelBuffer buffer = prepareBuffer(response);
        
        buffer.writeShort(response.getStatus());
        
        return buffer;
    }


    private Object encodeReportStatusRequest(ReportStatusRequest request) {
        ChannelBuffer buffer = prepareBuffer(request);
        
        buffer.writeShort(request.getStatus());
        
        return buffer;
    }
    
    private Object encodeReportStatusResponse(ReportStatusResponse response) {
        ChannelBuffer buffer = prepareBuffer(response);
        
        return buffer;
    }

    private Object encodeHelloRequest(HelloRequest request) throws UnsupportedEncodingException {
        ChannelBuffer buffer = prepareBuffer(request);
        
        writeStringUTF(buffer, request.getDeviceId());  // deviceId
        writeStringUTF(buffer, request.getDeviceMode());// deviceMode
        
        return buffer;
    }

    private Object encodeHelloResponse(HelloResponse response) throws UnsupportedEncodingException {
        ChannelBuffer buffer = prepareBuffer(response);
        
    	buffer.writeShort(response.getResultCode());
    	writeStringUTF(buffer, response.getServerName());
        
    	return buffer;
    }
    
    private static ChannelBuffer prepareBuffer(BaseMessage message) {
        ChannelBuffer buffer = ChannelBuffers.buffer(getChannelBufferSize(message));
        
        writeHeader(buffer, message);
        
        writeMessageId(buffer, message);
        
        return buffer;
    }
    
    private static int getChannelBufferSize(BaseMessage message) {
        return HEADER_SIZE + message.size();
    }

    private static void writeHeader(ChannelBuffer buffer, BaseMessage baseMessage) {
		buffer.writeShort(baseMessage.getMessageType().getValue());  // type
        buffer.writeShort(baseMessage.size());                   // length
	}
    
    private static void writeMessageId(ChannelBuffer buffer, BaseMessage baseMessage) {
        buffer.writeInt(baseMessage.getMessageId());                 // message id
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
