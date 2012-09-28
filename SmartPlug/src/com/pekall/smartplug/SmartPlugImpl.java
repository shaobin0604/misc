
package com.pekall.smartplug;

import com.pekall.smartplug.codec.SmartPlugDecoder;
import com.pekall.smartplug.codec.SmartPlugEncoder;
import com.pekall.smartplug.message.BaseMessage;
import com.pekall.smartplug.message.GetStatusResponse;
import com.pekall.smartplug.message.Heartbeat;
import com.pekall.smartplug.message.HelloRequest;
import com.pekall.smartplug.message.HelloResponse;
import com.pekall.smartplug.message.ReportStatusRequest;
import com.pekall.smartplug.message.ReportStatusResponse;
import com.pekall.smartplug.message.SetStatusRequest;
import com.pekall.smartplug.message.SetStatusResponse;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SmartPlugImpl implements SmartPlug {
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final short STATUS_ON = 1;
    private static final short STATUS_OFF = 0;
    
    private static final short RESULT_OK = 0;
    private static final short RESULT_ERR = 1;

    private SmartPlugListener mListener;
    private ClientBootstrap mClientBootstrap;
    private Channel mClientChannel;
    private AtomicInteger mCounter;
    private ScheduledExecutorService mHeartbeatScheduler;
    private ScheduledFuture<?> mHeartbeatFuture;
    

    public SmartPlugImpl(SmartPlugListener listener) {
        super();
        if (listener == null) {
            throw new IllegalArgumentException("listener should be be null");
        }
        mListener = listener;
        mCounter = new AtomicInteger();

        // Set up.
        mClientBootstrap = new ClientBootstrap(
                new OioClientSocketChannelFactory(
                        Executors.newCachedThreadPool()));

        // Configure the event pipeline factory.
        mClientBootstrap.setOption("connectTimeoutMillis", CONNECT_TIMEOUT_MS); // 10s
        mClientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new SmartPlugDecoder());
                pipeline.addLast("encoder", new SmartPlugEncoder());
                pipeline.addLast("handler", new ClientHandler());
                return pipeline;
            }
        });
    }

    public SmartPlugListener getListener() {
        return mListener;
    }

    public Channel getChannel() {
        return mClientChannel;
    }

    public int getNextMessageId() {
        return mCounter.getAndIncrement();
    }

    @Override
    public boolean connect(String host, int port) {
        clog("connect E");
        if (isConnected()) {
            clog("already connected. nop");
            return true;
        } else {
            // Make a new connection.
            ChannelFuture connectFuture = mClientBootstrap.connect(new InetSocketAddress(host, port));

            // Wait until the connection is made successfully.
            connectFuture.awaitUninterruptibly();

            if (connectFuture.isSuccess()) {
                mClientChannel = connectFuture.getChannel();
                clog("connect ok");
                return true;
            } else {
                clog("connect fail");
                return false;
            }
        }
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            mClientChannel.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        return mClientChannel != null && mClientChannel.isConnected();
    }

    @Override
    public boolean login(String pn, String sn) {
        clog("login E");
        ClientHandler handler = mClientChannel.getPipeline().get(ClientHandler.class);

        HelloRequest request = new HelloRequest(getNextMessageId(), sn, pn);
        HelloResponse response = (HelloResponse) handler.getResponse(request);

        boolean success = (response.getResultCode() == 0);
        if (success) {
            startHeartbeat();
        }
        clog("login X");
        return success;
    }

    private void startHeartbeat() {
        clog("startHeartbeat E");
        if (isConnected()) {
            mHeartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
            mHeartbeatFuture = mHeartbeatScheduler.scheduleAtFixedRate(new HeartbeatTimerTask(), 0, 10, TimeUnit.SECONDS);
        } else {
            clog("Error not connected");
        }
        clog("startHeartbeat X");
    }

    private void stopHeartbeat() {
        clog("stopHearbeat E");
        if (mHeartbeatScheduler != null && !mHeartbeatScheduler.isShutdown()) {
            if (mHeartbeatFuture != null) {
                mHeartbeatFuture.cancel(true);
            }
            mHeartbeatScheduler.shutdown();
        }
        clog("stopHeartbeat X");
    }

    @Override
    public boolean reportStatus(boolean on) {
        ClientHandler handler = mClientChannel.getPipeline().get(ClientHandler.class);
        ReportStatusRequest request = new ReportStatusRequest(getNextMessageId(), (on ? STATUS_ON : STATUS_OFF));
        ReportStatusResponse response = (ReportStatusResponse) handler.getResponse(request);
        return true;
    }

    @Override
    public void release() {
        stopHeartbeat();

        clog(">>>>> releaseExternalResources");
        mClientBootstrap.releaseExternalResources();
        clog("<<<<< releaseExternalResources");
    }

    private static void clog(String msg) {
        System.out.println("client --> " + msg);
    }

    private class ErrorEventCaller implements Runnable {
        private ExceptionEvent mExceptionEvent;

        public ErrorEventCaller(ExceptionEvent e) {
            mExceptionEvent = e;
        }

        @Override
        public void run() {
            SmartPlugImpl smartPlugImpl = SmartPlugImpl.this;
            smartPlugImpl.mListener.onError(smartPlugImpl, mExceptionEvent.toString());
            mExceptionEvent.getChannel().close();
        }
    }

    private class DisconnectedEventCaller implements Runnable {

        @Override
        public void run() {
            SmartPlugImpl smartPlugImpl = SmartPlugImpl.this;

            smartPlugImpl.stopHeartbeat();

            smartPlugImpl.mListener.onDisconnected(smartPlugImpl);
        }

    }

    private class InBoundMessageCaller implements Runnable {
        private BaseMessage mMessage;

        public InBoundMessageCaller(BaseMessage message) {
            this.mMessage = message;
        }

        @Override
        public void run() {
            switch (mMessage.getMessageType()) {
                case MSG_GET_STATUS_REQ:
                    onGetStatusRequested();
                    break;
                case MSG_SET_STATUS_REQ:
                    onSetStatusRequested();
                    break;

                default:
                    // ignore unknown message
                    break;
            }
        }

        private void onSetStatusRequested() {
            SetStatusRequest request = (SetStatusRequest) mMessage;
            final SmartPlugImpl smartPlugImpl = SmartPlugImpl.this;
            final SmartPlugListener listener = smartPlugImpl.mListener;
            boolean ret = listener.onSetStatusRequested(smartPlugImpl, request.getStatus() == STATUS_ON);
            SetStatusResponse response = new SetStatusResponse(mMessage.getMessageId(), ret ? RESULT_OK : RESULT_ERR);
            smartPlugImpl.mClientChannel.write(response);
        }

        private void onGetStatusRequested() {
            final SmartPlugImpl smartPlugImpl = SmartPlugImpl.this;
            final SmartPlugListener listener = smartPlugImpl.mListener;
            boolean status = listener.onGetStatusRequested(smartPlugImpl);
            GetStatusResponse response = new GetStatusResponse(mMessage.getMessageId(), status ? STATUS_ON : STATUS_OFF);
            smartPlugImpl.mClientChannel.write(response);
        }

    }

    private class HeartbeatTimerTask implements Runnable {

        @Override
        public void run() {
            final SmartPlugImpl smartPlugImpl = SmartPlugImpl.this;

            if (smartPlugImpl.isConnected()) {
                final SmartPlugListener listener = smartPlugImpl.mListener;
                boolean status = listener.onGetStatusRequested(smartPlugImpl);
                Heartbeat heartbeat = new Heartbeat(getNextMessageId(), status ? STATUS_ON : STATUS_OFF);
                smartPlugImpl.mClientChannel.write(heartbeat);

                clog("HeartbeatTimerTask send heartbeat");
            } 
        }
    }

    private class ClientHandler extends SimpleChannelUpstreamHandler {
        // Stateful properties
        private volatile Channel mChannel;
        private final BlockingQueue<BaseMessage> mAnswerQueue = new LinkedBlockingQueue<BaseMessage>();
        private ExecutorService mExecutor = Executors.newCachedThreadPool();

        public BaseMessage getResponse(BaseMessage request) {

            mChannel.write(request);

            boolean interrupted = false;
            BaseMessage response;
            for (;;) {
                try {
                    clog("before take");
                    response = mAnswerQueue.take();
                    clog("after take");
                    if (response.getMessageId() == request.getMessageId()) {
                        // got the matched response
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }

            return response;
        }

        @Override
        public void handleUpstream(
                ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
            if (e instanceof ChannelStateEvent) {
                clog(e.toString());
            }
            super.handleUpstream(ctx, e);
        }

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            mChannel = e.getChannel();
            clog("channelOpen");
            super.channelOpen(ctx, e);
        }

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            clog("channelDisconnected");
            mExecutor.execute(new DisconnectedEventCaller());
            super.channelDisconnected(ctx, e);
        }

        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            clog("channelClosed");
            super.channelClosed(ctx, e);
            mExecutor.shutdown();
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            BaseMessage message = (BaseMessage) e.getMessage();
            
            clog("messageReceived --> " + message.toString());

            switch (message.getMessageType()) {
                case MSG_GET_STATUS_REQ:
                case MSG_SET_STATUS_REQ: {
                    mExecutor.execute(new InBoundMessageCaller(message));
                    break;
                }

                case MSG_HELLO_RES:
                case MSG_REPORT_STATUS_RES: {
                    boolean offered = mAnswerQueue.offer(message);
                    assert offered;
                    break;
                }

                default:
                    break;
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            clog("exceptionCaught: " + e);
            mExecutor.execute(new ErrorEventCaller(e));
        }
    }

}
