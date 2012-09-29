package com.pekall.smartplug.codec;

import com.pekall.smartplug.message.GetStatusRequest;
import com.pekall.smartplug.message.GetStatusResponse;
import com.pekall.smartplug.message.Heartbeat;
import com.pekall.smartplug.message.HelloRequest;
import com.pekall.smartplug.message.HelloResponse;
import com.pekall.smartplug.message.MessageType;
import com.pekall.smartplug.message.ReportStatusRequest;
import com.pekall.smartplug.message.ReportStatusResponse;
import com.pekall.smartplug.message.SetStatusRequest;
import com.pekall.smartplug.message.SetStatusResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.io.UnsupportedEncodingException;

public class SmartPlugDecoder extends FrameDecoder {
	private static final int HEADER_SIZE = 4;   // msg_type: short, msg_length: short
    private static final int STRING_MAX_BYTES = 31;
    private static final int STRING_MAX_BYTES_PLUS_ONE = STRING_MAX_BYTES + 1;
	
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
    	// Make sure if the message header was received.
        if (buffer.readableBytes() < HEADER_SIZE) {
           // The message header was not received yet - return null.
           // This method will be invoked again when more packets are
           // received and appended to the buffer.
           return null;
        }

        // The message header is in the buffer.

        // Mark the current buffer position before reading the type field
        // because the whole frame might not be in the buffer yet.
        // We will reset the buffer position to the marked position if
        // there's not enough bytes in the buffer.
        buffer.markReaderIndex();

        // Read the type field;
        short typeValue = buffer.readShort();
        // Read the length field.
        short length = buffer.readShort();

        // Make sure if there's enough bytes in the buffer.
        if (buffer.readableBytes() < length) {
           // The whole bytes were not received yet - return null.
           // This method will be invoked again when more packets are
           // received and appended to the buffer.

           // Reset to the marked position to read the type field again
           // next time.
           buffer.resetReaderIndex();

           return null;
        }

        MessageType type = MessageType.fromValue(typeValue);
        
        switch (type) {
            case MSG_HELLO_REQ:
                return decodeHelloRequest(buffer);
            case MSG_HELLO_RES:
                return decodeHelloResponse(buffer);
            case MSG_REPORT_STATUS_REQ:
                return decodeReportStatusRequest(buffer);
            case MSG_REPORT_STATUS_RES:
                return decodeReportStatusResponse(buffer);
            case MSG_HEARTBEAT:
                return decodeHeartbeat(buffer);
            case MSG_GET_STATUS_REQ:
                return decodeGetStatusRequest(buffer);
            case MSG_GET_STATUS_RES:
                return decodeGetStatusResponse(buffer);
            case MSG_SET_STATUS_REQ:
                return decodeSetStatusRequest(buffer);
            case MSG_SET_STATUS_RES:
                return decodeSetStatusResponse(buffer);
            default:
                throw new IllegalArgumentException("Unknown MessageType: " + type);
        }
    }

    private Object decodeHeartbeat(ChannelBuffer buffer) {
        int messageId = buffer.readInt();
        short status = buffer.readShort();
        return new Heartbeat(messageId, status);
    }

    private Object decodeSetStatusResponse(ChannelBuffer buffer) {
        int messageId = buffer.readInt();
        short resultCode = buffer.readShort();
        return new SetStatusResponse(messageId, resultCode);
    }

    private Object decodeSetStatusRequest(ChannelBuffer buffer) {
        int messageId = buffer.readInt();
        short status = buffer.readShort();
        return new SetStatusRequest(messageId, status);
    }

    private Object decodeGetStatusResponse(ChannelBuffer buffer) {
        int messageId = buffer.readInt();
        short status = buffer.readShort();
        return new GetStatusResponse(messageId, status);
    }

    private Object decodeGetStatusRequest(ChannelBuffer buffer) {
        int messageId = buffer.readInt();
        return new GetStatusRequest(messageId);
    }

    private Object decodeReportStatusResponse(ChannelBuffer buffer) {
        int messageId = buffer.readInt();
        return new ReportStatusResponse(messageId);
    }

    private Object decodeReportStatusRequest(ChannelBuffer buffer) {
        int messageId = buffer.readInt();
        short status = buffer.readShort();
        return new ReportStatusRequest(messageId, status);
    }

    private Object decodeHelloRequest(ChannelBuffer buffer) throws UnsupportedEncodingException {
        int messageId = buffer.readInt();

        byte[] bytes = new byte[STRING_MAX_BYTES_PLUS_ONE];
    	int deviceIdLen = buffer.bytesBefore((byte)0);
    	buffer.readBytes(bytes);
    	String deviceId = new String(bytes, 0, deviceIdLen, "utf-8");
    	
    	int deviceModeLen = buffer.bytesBefore((byte)0);
    	buffer.readBytes(bytes);
    	String deviceMode = new String(bytes, 0, deviceModeLen, "utf-8");
    	
    	return new HelloRequest(messageId, deviceId, deviceMode);
	}

	private Object decodeHelloResponse(ChannelBuffer buffer) throws UnsupportedEncodingException {
	    int messageId = buffer.readInt();
	    
		short resultCode = buffer.readShort();
		
		int serverNameLen = buffer.bytesBefore((byte)0);
		byte[] bytes = new byte[STRING_MAX_BYTES_PLUS_ONE];
		buffer.readBytes(bytes);
		String serverName = new String(bytes, 0, serverNameLen, "utf-8");
		
		return new HelloResponse(messageId, resultCode, serverName);
	}
}
