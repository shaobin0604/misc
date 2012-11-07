
package com.pekall.pctool;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_0;

import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Environment;

import com.pekall.pctool.model.HandlerFacade;
import com.pekall.pctool.model.app.AppUtil;
import com.pekall.pctool.model.app.AppUtil.AppNotExistException;
import com.pekall.pctool.protos.MsgDefProtos.CmdRequest;
import com.pekall.pctool.protos.MsgDefProtos.CmdResponse;
import com.pekall.pctool.protos.MsgDefProtos.CmdType;
import com.pekall.pctool.util.Slog;
import com.pekall.pctool.util.StorageUtil;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.DiskAttribute;
import org.jboss.netty.handler.codec.http.multipart.DiskFileUpload;
import org.jboss.netty.handler.codec.http.multipart.FileUpload;
import org.jboss.netty.handler.codec.http.multipart.HttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.NotEnoughDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import org.jboss.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HttpServerHandler extends SimpleChannelUpstreamHandler {

    // server error code

    private static final int RPC_END_POINT = 1;
    private static final int EXPORT_APP = 2;
    private static final int IMPORT_APP = 3;

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI("localhost", "rpc", RPC_END_POINT);
        sURIMatcher.addURI("localhost", "export/*", EXPORT_APP);
        sURIMatcher.addURI("localhost", "import", IMPORT_APP);
    }
    
    //
    // APK file upload related
    // 
    private HttpRequest request;

    private boolean readingChunks;

    private final StringBuilder responseContent = new StringBuilder();

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(
            DefaultHttpDataFactory.MINSIZE); // Disk if size exceed MINSIZE

    private HttpPostRequestDecoder decoder;
    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
                                                         // on exit (in normal
                                                         // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
                                                        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }
    
    private ShutdownServerListener mShutdownServerListener;

    //
    // Business logic facade
    //
    private HandlerFacade mHandlerFacade;

    public HttpServerHandler(HandlerFacade facade) {
        this.mHandlerFacade = facade;
        this.mShutdownServerListener = new ShutdownServerListener(facade.getContext());
    }
    
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelOpen(ctx, e);
        
        // see 
        // * http://static.netty.io/3.5/guide/#start.12
        // * http://stackoverflow.com/questions/10950244/server-bootstrap-releaseexternalresources-stuck-in-loop
        // * http://biasedbit.com/netty-releaseexternalresources-hangs/
        HttpServer.sAllChannels.add(e.getChannel());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        
//        Slog.d("MessageReceived, e.getClass: " + e.getMessage().getClass().toString() + ", readingChunks: " + readingChunks);
        
        if (readingChunks) {
            handleImportApp(ctx, e);
            return;
        }
        
        HttpRequest request = (HttpRequest) e.getMessage();
        
        String path = request.getUri();
        HttpMethod method = request.getMethod();
        path = sanitizeUri(path);

        Slog.d("path:" + path + ", method: " + method);
        

        Uri url = Uri.parse("content://localhost" + path);

        int match = sURIMatcher.match(url);

        Slog.d("url = " + url);
        Slog.d("match = " + match);

        switch (match) {
            case RPC_END_POINT: {
                if (HttpMethod.POST.equals(method)) {
                    HttpResponseWrapper httpResponseWrapper = handleRPC(request);
                    Channel ch = e.getChannel();
                    ChannelFuture future = ch.write(httpResponseWrapper.mHttpResponse);
                    if (httpResponseWrapper.mIsHaltRequested) {
                        future.addListener(mShutdownServerListener);
                    } else {
                        future.addListener(ChannelFutureListener.CLOSE);
                    }
                } else {
                    Slog.e("not http post request");
                    sendError(ctx, BAD_REQUEST);
                }
                break;
            }
            case EXPORT_APP: {
                if (HttpMethod.GET.equals(method)) {
                    String packageName = url.getPathSegments().get(1);
                    handleExportApp(packageName, e);
                } else {
                    sendError(ctx, BAD_REQUEST);
                }
                break;
            }
            case IMPORT_APP: {
                if (HttpMethod.POST.equals(method)) {
                    handleImportApp(ctx, e);
                } else {
                    sendError(ctx, BAD_REQUEST);
                }
                break;
            }

            default: {
                sendError(ctx, NOT_FOUND);
                break;
            }
        }

    }

    private HttpResponseWrapper handleRPC(HttpRequest request) {
        Slog.d("\n==================== handleRPC E ====================\n\n");

        String contentLength = request.getHeader(CONTENT_LENGTH);
        
        Slog.d("header: Content-Length = " + contentLength);
        
        boolean isChunked = request.isChunked();
        
        Slog.d("isChunked: " + isChunked);
        
        ChannelBuffer content = request.getContent();
        
        Slog.d("body: length = " + content.readableBytes());
        
        ChannelBufferInputStream cbis = new ChannelBufferInputStream(content);
        HttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_0, OK);
        boolean shutdownServer = false;
        try {
            CmdRequest cmdRequest = CmdRequest.parseFrom(cbis);
            
            CmdResponse cmdResponse = mHandlerFacade.handleCmdRequest(cmdRequest);
            
            if (cmdResponse.getCmdType() == CmdType.CMD_DISCONNECT) {
                shutdownServer = true;
            }
            
            ChannelBuffer buffer = new DynamicChannelBuffer(2048);
            buffer.writeBytes(cmdResponse.toByteArray());
            
            httpResponse.setContent(buffer);
            httpResponse.setHeader(CONTENT_TYPE, "application/x-protobuf");
            httpResponse.setHeader(CONTENT_LENGTH, httpResponse.getContent().writerIndex());
        } catch (IOException e) {
            Slog.e("Error when handleRPC, send 500 internal server error", e);

            httpResponse.setStatus(INTERNAL_SERVER_ERROR);
            httpResponse.setHeader(CONTENT_LENGTH, 0);
        }
        HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper(httpResponse, shutdownServer);
        Slog.d("\n=====================================================\n\n");
        return httpResponseWrapper;
    }

    private void handleExportApp(final String packageName, MessageEvent event) {
        Slog.d("packageName = " + packageName);
        try {
            InputStream is = mHandlerFacade.exportApp(packageName);
            if (is instanceof FileInputStream) {
                Slog.d("use zero-copy");

                HttpResponse response = new DefaultHttpResponse(HTTP_1_0, OK);

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
                        future.getChannel().close();
                    }

                    public void operationProgressed(
                            ChannelFuture future, long amount, long current, long total) {
                        Slog.d(String.format("%s: %d / %d (+%d)%n", packageName, current, total, amount));
                    }
                });
                
            }
        } catch (AppNotExistException e) {
            Slog.e("Error app not exist", e);
            HttpResponse response = new DefaultHttpResponse(HTTP_1_0, NOT_FOUND);

            response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.setContent(ChannelBuffers.copiedBuffer(
                    "Failure: " + NOT_FOUND.toString() + "\r\n",
                    CharsetUtil.UTF_8));

            // Close the connection as soon as the error message is sent.
            event.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        } catch (IOException e) {
            Slog.e("Error when export app", e);
            HttpResponse response = new DefaultHttpResponse(HTTP_1_0, INTERNAL_SERVER_ERROR);

            response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.setContent(ChannelBuffers.copiedBuffer(
                    "Failure: " + INTERNAL_SERVER_ERROR.toString() + "\r\n",
                    CharsetUtil.UTF_8));

            // Close the connection as soon as the error message is sent.
            event.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void handleImportApp(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!StorageUtil.isSdCardMounted()) {
            Slog.e("Error sdcard not mounted");
            sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
            return;
        }
        
        if (!readingChunks) {
            // clean previous FileUpload if Any
            if (decoder != null) {
                decoder.cleanFiles();
                decoder = null;
            }
            HttpRequest request = this.request = (HttpRequest) e.getMessage();
            
            responseContent.setLength(0);
            responseContent.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            responseContent.append("===================================\r\n");

            responseContent.append("VERSION: " +
                    request.getProtocolVersion().getText() + "\r\n");

            responseContent.append("REQUEST_URI: " + request.getUri() +
                    "\r\n\r\n");
            responseContent.append("\r\n\r\n");

            // new method
            List<Entry<String, String>> headers = request.getHeaders();
            for (Entry<String, String> entry: headers) {
                responseContent.append("HEADER: " + entry.getKey() + "=" +
                        entry.getValue() + "\r\n");
            }
            responseContent.append("\r\n\r\n");

            // new method
            Set<Cookie> cookies;
            String value = request.getHeader(HttpHeaders.Names.COOKIE);
            if (value == null) {
                cookies = Collections.emptySet();
            } else {
                CookieDecoder decoder = new CookieDecoder();
                cookies = decoder.decode(value);
            }
            for (Cookie cookie: cookies) {
                responseContent.append("COOKIE: " + cookie.toString() + "\r\n");
            }
            responseContent.append("\r\n\r\n");

            QueryStringDecoder decoderQuery = new QueryStringDecoder(request
                    .getUri());
            Map<String, List<String>> uriAttributes = decoderQuery
                    .getParameters();
            for (String key: uriAttributes.keySet()) {
                for (String valuen: uriAttributes.get(key)) {
                    responseContent.append("URI: " + key + "=" + valuen +
                            "\r\n");
                }
            }
            responseContent.append("\r\n\r\n");

            // if GET Method: should not try to create a HttpPostRequestDecoder
            try {
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (ErrorDataDecoderException e1) {
                e1.printStackTrace();
                responseContent.append(e1.getMessage());
                writeResponse(e.getChannel());
                Channels.close(e.getChannel());
                return;
            } catch (IncompatibleDataDecoderException e1) {
                // GET Method: should not try to create a HttpPostRequestDecoder
                // So OK but stop here
                responseContent.append(e1.getMessage());
                responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");
                writeResponse(e.getChannel());
                return;
            }

            responseContent.append("Is Chunked: " + request.isChunked() +
                    "\r\n");
            responseContent.append("IsMultipart: " + decoder.isMultipart() +
                    "\r\n");
            if (request.isChunked()) {
                // Chunk version
                responseContent.append("Chunks: ");
                readingChunks = true;
            } else {
                // Not chunk version
                readHttpDataAllReceive(e.getChannel());
                responseContent
                        .append("\r\n\r\nEND OF NOT CHUNKED CONTENT\r\n");
                writeResponse(e.getChannel());
            }
        } else {
            // New chunk is received
            HttpChunk chunk = (HttpChunk) e.getMessage();
            try {
                decoder.offer(chunk);
            } catch (ErrorDataDecoderException e1) {
                e1.printStackTrace();
                responseContent.append(e1.getMessage());
                writeResponse(e.getChannel());
                Channels.close(e.getChannel());
                return;
            }
            responseContent.append("o");
            // example of reading chunk by chunk (minimize memory usage due to Factory)
//          readHttpDataChunkByChunk(e.getChannel());
            // example of reading only if at the end
            if (chunk.isLast()) {
                readHttpDataAllReceive(e.getChannel());
                writeResponse(e.getChannel());
                readingChunks = false;
            }
        }
        
    }
    
    /**
     * Example of reading all InterfaceHttpData from finished transfer
     *
     * @param channel
     */
    private void readHttpDataAllReceive(Channel channel) {
        List<InterfaceHttpData> datas = null;
        try {
            datas = decoder.getBodyHttpDatas();
        } catch (NotEnoughDataDecoderException e1) {
            // Should not be!
            e1.printStackTrace();
            responseContent.append(e1.getMessage());
            writeResponse(channel);
            Channels.close(channel);
            return;
        }
        for (InterfaceHttpData data: datas) {
            writeHttpData(data);
        }
        responseContent.append("\r\n\r\nEND OF CONTENT AT FINAL END\r\n");
    }

    /**
     * Example of reading request by chunk and getting values from chunk to
     * chunk
     *
     * @param channel
     */
    private void readHttpDataChunkByChunk(Channel channel) {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    // new value
                    writeHttpData(data);
                }
            }
        } catch (EndOfDataDecoderException e1) {
            // end
            responseContent
                    .append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
        }
    }

    private void writeHttpData(InterfaceHttpData data) {
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
                // Error while reading data from File, only print name and error
                e1.printStackTrace();
                responseContent.append("\r\nBODY Attribute: " +
                        attribute.getHttpDataType().name() + ": " +
                        attribute.getName() + " Error while reading value: " +
                        e1.getMessage() + "\r\n");
                return;
            }
            if (value.length() > 100) {
                responseContent.append("\r\nBODY Attribute: " +
                        attribute.getHttpDataType().name() + ": " +
                        attribute.getName() + " data too long\r\n");
            } else {
                responseContent.append("\r\nBODY Attribute: " +
                        attribute.getHttpDataType().name() + ": " +
                        attribute.toString() + "\r\n");
            }
        } else {
            responseContent.append("\r\nBODY FileUpload: " +
                    data.getHttpDataType().name() + ": " + data.toString() +
                    "\r\n");
            if (data.getHttpDataType() == HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    if (fileUpload.length() < 10000) {
                        responseContent.append("\tContent of file\r\n");
                        try {
                            responseContent
                                    .append(((FileUpload) data)
                                            .getString(((FileUpload) data)
                                                    .getCharset()));
                        } catch (IOException e1) {
                            // do nothing for the example
                            e1.printStackTrace();
                        }
                        responseContent.append("\r\n");
                    } else {
                        responseContent
                                .append("\tFile too long to be printed out:" +
                                        fileUpload.length() + "\r\n");
                    }
                    boolean isInMemory = fileUpload.isInMemory();// tells if the file is in Memory or on File
                    
                    Slog.d("isInMemory: " + isInMemory);
                    
                    Context context = mHandlerFacade.getContext();
                    
                    File externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    
                    File dest = new File(externalDir, fileUpload.getFilename());
                    
                    Slog.d("dest: " + dest);
                    
                    try {
                        fileUpload.renameTo(dest); // enable to move into another File dest
                        AppUtil.installAPK(context, dest);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } 
                    //decoder.removeFileUploadFromClean(fileUpload); //remove the File of to delete file
                    decoder.removeHttpDataFromClean(data);
                } else {
                    responseContent
                            .append("\tFile to be continued but should not!\r\n");
                }
            }
        }
    }

    private void writeResponse(Channel channel) {
        // Convert the response content to a ChannelBuffer.
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent
                .toString(), CharsetUtil.UTF_8);
        responseContent.setLength(0);

        // Decide whether to close the connection or not.
        boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request
                .getHeader(HttpHeaders.Names.CONNECTION)) ||
                request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) &&
                !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request
                        .getHeader(HttpHeaders.Names.CONNECTION));

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.OK);
        response.setContent(buf);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
                "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String
                    .valueOf(buf.readableBytes()));
        }

        Set<Cookie> cookies;
        String value = request.getHeader(HttpHeaders.Names.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            CookieDecoder decoder = new CookieDecoder();
            cookies = decoder.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            CookieEncoder cookieEncoder = new CookieEncoder(true);
            for (Cookie cookie: cookies) {
                cookieEncoder.addCookie(cookie);
                response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder
                        .encode());
                cookieEncoder = new CookieEncoder(true);
            }
        }
        // Write the response.
        ChannelFuture future = channel.write(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
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
        HttpResponse response = new DefaultHttpResponse(HTTP_1_0, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(
                "Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response)
                .addListener(ChannelFutureListener.CLOSE);
    }
    
    private static final class HttpResponseWrapper {
        boolean mIsHaltRequested;    // whether shutdown server after send response 
        HttpResponse mHttpResponse;
        
        HttpResponseWrapper(HttpResponse httpResponse, boolean isHaltRequested) {
            mHttpResponse = httpResponse;
            mIsHaltRequested = isHaltRequested;
        }
    }
    
    private static final class ShutdownServerListener implements ChannelFutureListener {
        
        private Context mContext;
        
        ShutdownServerListener(Context context) {
            mContext = context;
        }
        
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            // 1. close channel first
            future.getChannel().close();
            
            // 2. then send broadcast to shutdown server
            Intent shutdownIntent = new Intent(AmCommandReceiver.ACTION_MAIN_SERVER_STOP);
            mContext.sendBroadcast(shutdownIntent);
        }
    }
}
