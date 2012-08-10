package com.pekall.pctool;



import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.pekall.pctool.protos.MsgDefProtos.AppRecord;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MainServerHandler extends SimpleChannelUpstreamHandler {

    private static final int RPC_END_POINT = 1;
   
    private FakeBusinessLogicFacade mLogicFacade;

    public MainServerHandler(FakeBusinessLogicFacade facade) {
        this.mLogicFacade = facade;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();

        String path = request.getUri();
        HttpMethod method = request.getMethod();

        path = sanitizeUri(path);

        Slog.d("path:" + path + ", method: " + method);

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

        handleRpc(request, response);

        Channel ch = e.getChannel();
        // Write the initial line and the header.
        ChannelFuture future = ch.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    private void handleRpc(HttpRequest request, HttpResponse response) {
        
        Slog.d("handleRpc");
        
        Slog.d("content length = " + request.getHeader(CONTENT_LENGTH));
        Slog.d("content type = " + request.getHeader(CONTENT_TYPE));
        
        ChannelBuffer content = request.getContent();
        
        Slog.d("content length = " + content.readableBytes());
        
        ChannelBufferInputStream cbis = new ChannelBufferInputStream(content);
        
        try {
            CmdRequest cmdRequest = CmdRequest.parseFrom(cbis);
            CmdType cmdType = cmdRequest.getType();
            
            Slog.d("cmdType = " + cmdType);
            switch (cmdType) {
                case CMD_QUERY_APP:
                    AppRecord appRecord = cmdRequest.getAppParams();
                    if (appRecord != null) {
                        Slog.d("type = " + appRecord.getType());
                        Slog.d("location = " + appRecord.getLocation());
                    }
                    
                    CmdResponse cmdResponse = mLogicFacade.queryAppRecordList();
                    
                    ChannelBuffer buffer = new DynamicChannelBuffer(2048);
                    buffer.writeBytes(cmdResponse.toByteArray());

                    response.setContent(buffer);
                    response.setHeader(CONTENT_TYPE, "application/x-protobuf");
                    response.setHeader(CONTENT_LENGTH, response.getContent().writerIndex());
                    break;

                default:
                    break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void handleNotFound(HttpRequest request, HttpResponse response) {
        response.setStatus(NOT_FOUND);
    }

    private static String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        return uri;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(
                "Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response)
                .addListener(ChannelFutureListener.CLOSE);
    }
}
