package com.pekall.smartplug.codec;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.pekall.smartplug.message.HelloRequest;
import com.pekall.smartplug.message.HelloResponse;

import static com.pekall.smartplug.message.MessageType.MSG_HELLO_REQ;
import static com.pekall.smartplug.message.MessageType.MSG_HELLO_RES;
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
        short type = buffer.readShort();
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

        if (type == MSG_HELLO_REQ.getValue()) {
			return decodeHelloRequest(buffer);
        } else if (type == MSG_HELLO_RES.getValue()) {
			return decodeHelloResponse(buffer);
        } else {
			throw new IllegalArgumentException();
		}
    }

    private Object decodeHelloRequest(ChannelBuffer buffer) throws UnsupportedEncodingException {
    	byte[] bytes = new byte[STRING_MAX_BYTES_PLUS_ONE];
    	int deviceIdLen = buffer.bytesBefore((byte)0);
    	buffer.readBytes(bytes);
    	String deviceId = new String(bytes, 0, deviceIdLen, "utf-8");
    	
    	int deviceModeLen = buffer.bytesBefore((byte)0);
    	buffer.readBytes(bytes);
    	String deviceMode = new String(bytes, 0, deviceModeLen, "utf-8");
    	
    	return new HelloRequest(deviceId, deviceMode);
	}

	private Object decodeHelloResponse(ChannelBuffer buffer) throws UnsupportedEncodingException {
		short resultCode = buffer.readShort();
		int serverNameLen = buffer.bytesBefore((byte)0);
		byte[] bytes = new byte[STRING_MAX_BYTES_PLUS_ONE];
		buffer.readBytes(bytes);
		String serverName = new String(bytes, 0, serverNameLen, "utf-8");
		
		return new HelloResponse(resultCode, serverName);
	}

	

}
