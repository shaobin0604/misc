
package com.pekall.pctool;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import android.content.UriMatcher;
import android.net.Uri;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.pekall.pctool.model.HandlerFacade;
import com.pekall.pctool.model.app.AppUtil.AppNotExistException;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;
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
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;

public class MainServerHandler extends SimpleChannelUpstreamHandler {

    // server error code

    private static final int RPC_END_POINT = 1;
    private static final int APPS = 2;
    private static final int TEST = 3;
    private static final int EXPORT_APP = 4;
    private static final int IMPORT_APP = 5;

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI("localhost", "rpc", RPC_END_POINT);
        sURIMatcher.addURI("localhost", "apps", APPS);
        sURIMatcher.addURI("localhost", "test", TEST);
        sURIMatcher.addURI("localhost", "export/*", EXPORT_APP);
        sURIMatcher.addURI("localhost", "import/*", IMPORT_APP);
    }

    private HandlerFacade mHandlerFacade;

    public MainServerHandler(HandlerFacade facade) {
        this.mHandlerFacade = facade;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();

        String path = request.getUri();
        HttpMethod method = request.getMethod();

        path = sanitizeUri(path);

        Slog.d("path:" + path + ", method: " + method);

        Uri url = Uri.parse("content://localhost" + path);

        int match = sURIMatcher.match(url);

        Slog.d("url = " + url);
        Slog.d("match = " + match);

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

        switch (match) {
            case RPC_END_POINT: {
                if (HttpMethod.POST.equals(method)) {
                    handleRPC(request, response);
                } else {
                    Slog.e("not http post request");
                    response.setStatus(HttpResponseStatus.BAD_REQUEST);
                    response.setHeader(CONTENT_LENGTH, 0);
                }
                Channel ch = e.getChannel();
                // Write the initial line and the header.
                ChannelFuture future = ch.write(response);
                future.addListener(ChannelFutureListener.CLOSE);

                break;
            }
            case EXPORT_APP: {
                if (HttpMethod.GET.equals(method)) {
                    String packageName = url.getPathSegments().get(1);
                    handleExportApp(packageName, e);
                }
                break;
            }
            case IMPORT_APP: {
                if (HttpMethod.POST.equals(method)) {

                }
                break;
            }

            default: {
                response.setStatus(NOT_FOUND);
                Channel ch = e.getChannel();
                // Write the initial line and the header.
                ChannelFuture future = ch.write(response);
                future.addListener(ChannelFutureListener.CLOSE);
                break;
            }
        }

    }

    private void handleRPC(HttpRequest request, HttpResponse response) {

        Slog.d("handleRPC");

        ChannelBuffer content = request.getContent();

        ChannelBufferInputStream cbis = new ChannelBufferInputStream(content);

        try {
            CmdRequest cmdRequest = CmdRequest.parseFrom(cbis);

            if (cmdRequest.hasCmdType()) {
                CmdType cmdType = cmdRequest.getCmdType();
                CmdResponse cmdResponse;
                Slog.d("cmdType = " + cmdType);
                switch (cmdType) {
                    //
                    // HEARTBEAT related methods
                    //
                    case CMD_HEART_BEAT: {
                        cmdResponse = mHandlerFacade.heartbeat(cmdRequest);
                    }
                    
                    //
                    // APP related methods
                    //
                    case CMD_QUERY_APP: {
                        cmdResponse = mHandlerFacade.queryApp(cmdRequest);
                        break;
                    }

                    //
                    // SMS related methods
                    //
                    case CMD_QUERY_SMS: {
                        cmdResponse = mHandlerFacade.querySms(cmdRequest);
                        break;
                    }

                    case CMD_DELETE_SMS: {
                        cmdResponse = mHandlerFacade.deleteSms(cmdRequest);
                        break;
                    }

                    case CMD_IMPORT_SMS: {
                        cmdResponse = mHandlerFacade.importSms(cmdRequest);
                        break;
                    }

                    //
                    // Calendar related methods
                    //
                    case CMD_QUERY_CALENDAR: {
                        cmdResponse = mHandlerFacade.queryCalendar(cmdRequest);
                        break;
                    }
                    
                    case CMD_QUERY_AGENDAS: {
                        cmdResponse = mHandlerFacade.queryAgenda(cmdRequest);
                        break;
                    }

                    case CMD_ADD_AGENDA: {
                        cmdResponse = mHandlerFacade.addAgenda(cmdRequest);
                        break;
                    }
                    
                    case CMD_EDIT_AGENDA: {
                        cmdResponse = mHandlerFacade.updateAgenda(cmdRequest);
                        break;
                    }
                    
                    case CMD_DELETE_AGENDA: {
                        cmdResponse = mHandlerFacade.deleteAgenda(cmdRequest);
                        break;
                    }
                    
                    //
                    // Contact related methods
                    //
                    case CMD_GET_ALL_ACCOUNTS: {
                        cmdResponse = mHandlerFacade.queryAccount(cmdRequest);
                        break;
                    }
                    
                    case CMD_GET_ALL_GROUPS: {
                        cmdResponse = mHandlerFacade.queryGroup(cmdRequest);
                        break;
                    }
                    
                    case CMD_ADD_GROUP: {
                        cmdResponse = mHandlerFacade.addGroup(cmdRequest);
                        break;
                    }
                    
                    case CMD_EDIT_GROUP: {
                        cmdResponse = mHandlerFacade.updateGroup(cmdRequest);
                        break;
                    }
                    
                    case CMD_DELETE_GROUP: {
                        cmdResponse = mHandlerFacade.deleteGroup(cmdRequest);
                        break;
                    }
                    
                    default: {
                        cmdResponse = mHandlerFacade.defaultCmdResponse();
                        break;
                    }
                }
                ChannelBuffer buffer = new DynamicChannelBuffer(2048);
                buffer.writeBytes(cmdResponse.toByteArray());

                response.setContent(buffer);
                response.setHeader(CONTENT_TYPE, "application/x-protobuf");
                response.setHeader(CONTENT_LENGTH, response.getContent().writerIndex());
            } else {
                Slog.e("Error insufficient params: cmdType");
                
                response.setStatus(BAD_REQUEST);
                response.setHeader(CONTENT_LENGTH, 0);
            }
        } catch (IOException e) {
            Slog.e("Error when handleRPC, send 500 internal server error", e);

            response.setStatus(INTERNAL_SERVER_ERROR);
            response.setHeader(CONTENT_LENGTH, 0);
        }
    }

    private void handleExportApp(final String packageName, MessageEvent event) {
        Slog.d("packageName = " + packageName);
        try {
            InputStream is = mHandlerFacade.exportApp(packageName);
            if (is instanceof FileInputStream) {
                Slog.d("use zero-copy");

                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

                FileChannel src = ((FileInputStream) is).getChannel();

                response.setHeader(CONTENT_TYPE, "application/vnd.android.package-archive");
                long fileLength = src.size();
                response.setHeader(CONTENT_LENGTH, fileLength);

                Channel ch = event.getChannel();

                // Write the initial line and the header.
                ch.write(response);

                final FileRegion region =
                        new DefaultFileRegion(src, 0, fileLength);
                ChannelFuture writeFuture = ch.write(region);
                writeFuture.addListener(new ChannelFutureProgressListener() {
                    public void operationComplete(ChannelFuture future) {
                        region.releaseExternalResources();
                    }

                    public void operationProgressed(
                            ChannelFuture future, long amount, long current, long total) {
                        Slog.d(String.format("%s: %d / %d (+%d)%n", packageName, current, total, amount));
                    }
                });
            }
        } catch (AppNotExistException e) {
            Slog.e("Error app not exist", e);
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);

            response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.setContent(ChannelBuffers.copiedBuffer(
                    "Failure: " + NOT_FOUND.toString() + "\r\n",
                    CharsetUtil.UTF_8));

            // Close the connection as soon as the error message is sent.
            event.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        } catch (IOException e) {
            Slog.e("Error when export app", e);
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);

            response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.setContent(ChannelBuffers.copiedBuffer(
                    "Failure: " + INTERNAL_SERVER_ERROR.toString() + "\r\n",
                    CharsetUtil.UTF_8));

            // Close the connection as soon as the error message is sent.
            event.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void handleImportApp(final String packageName, MessageEvent e) {
        Slog.d("packageName = " + packageName);
        
        // TODO
    }

    private void handleQueryAppTest(CmdRequest cmdRequest, HttpResponse response) {
        AppRecord appRecord = cmdRequest.getAppParams();
        if (appRecord != null) {
            Slog.d("type = " + appRecord.getAppType());
            Slog.d("location = " + appRecord.getLocationType());
        }

        CmdResponse cmdResponse = mHandlerFacade.queryAppRecordList();

        ChannelBuffer buffer = new DynamicChannelBuffer(2048);
        buffer.writeBytes(cmdResponse.toByteArray());

        response.setContent(buffer);
        response.setHeader(CONTENT_TYPE, "application/x-protobuf");
        response.setHeader(CONTENT_LENGTH, response.getContent().writerIndex());
    }

    private void handleApps(HttpRequest request, HttpResponse response) {
        AppInfoPList appInfoPList = mHandlerFacade.getAppInfoPList();

        ChannelBuffer buffer = new DynamicChannelBuffer(2048);
        buffer.writeBytes(appInfoPList.toByteArray());

        response.setContent(buffer);
        response.setHeader(CONTENT_TYPE, "application/x-protobuf");
        response.setHeader(CONTENT_LENGTH, response.getContent().writerIndex());
    }

    private void handleTest(HttpRequest request, HttpResponse response) {
        AddressBook addressBook = mHandlerFacade.getAddressBook();

        ChannelBuffer buffer = new DynamicChannelBuffer(2048);
        buffer.writeBytes(addressBook.toByteArray());

        response.setContent(buffer);
        response.setHeader(CONTENT_TYPE, "application/x-protobuf");
        response.setHeader(CONTENT_LENGTH, response.getContent().writerIndex());
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
