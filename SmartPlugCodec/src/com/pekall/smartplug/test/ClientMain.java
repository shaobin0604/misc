
package com.pekall.smartplug.test;

import com.pekall.smartplug.codec.SmartPlugDecoder;
import com.pekall.smartplug.codec.SmartPlugEncoder;
import com.pekall.smartplug.message.BaseMessage;
import com.pekall.smartplug.message.HelloRequest;

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
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientMain {

    private static class ClientHandler extends SimpleChannelUpstreamHandler {

        // Stateful properties
        private volatile Channel channel;
        private final BlockingQueue<BaseMessage> answer = new LinkedBlockingQueue<BaseMessage>();

        public BaseMessage getResponse(BaseMessage request) {
            channel.write(request);

            boolean interrupted = false;
            BaseMessage response;
            for (;;) {
                try {
                    clog("before take");
                    response = answer.take();
                    clog("after take");
                    break;
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
            channel = e.getChannel();
            clog("channelOpen");
            super.channelOpen(ctx, e);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            BaseMessage message = (BaseMessage) e.getMessage();
            clog(message.toString());
            boolean offered = answer.offer(message);
            assert offered;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            clog("Unexpected exception from downstream.");
            e.getChannel().close();
        }
    }

    private static final int SERVER_PORT = 8888;
    private static ClientBootstrap sClientBootstrap;
    private static Channel sClientChannel;

    private static void clog(String msg) {
        System.out.println("client --> " + msg);
    }

    private static void start() {
        // Set up.
        sClientBootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Configure the event pipeline factory.
        sClientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new SmartPlugDecoder());
                pipeline.addLast("encoder", new SmartPlugEncoder());
                pipeline.addLast("handler", new ClientHandler());
                return pipeline;
            }
        });

        // Make a new connection.
        ChannelFuture connectFuture =
                sClientBootstrap.connect(new InetSocketAddress("localhost", SERVER_PORT));

        // Wait until the connection is made successfully.
        sClientChannel = connectFuture.awaitUninterruptibly().getChannel();
    }

    private static void stop() {
        // Close the connection.
        sClientChannel.close().awaitUninterruptibly();

        // Shut down all thread pools to exit.
        sClientBootstrap.releaseExternalResources();
    }

    public static void main(String[] args) {
        start();
        
        testHello();
    }

    private static void testHello() {
        // Get the handler instance to initiate the request.
        ClientHandler handler = sClientChannel.getPipeline().get(ClientHandler.class);

        int messageId = 0;
        String deviceId = "123456789";
        String deviceMode = "abcdefghi";

        HelloRequest request = new HelloRequest(messageId, deviceId, deviceMode);

        // Request and get the response.
        clog("before getResponse");
        BaseMessage response = handler.getResponse(request);
        clog("after getResponse");
        
        clog(response.toString());
    }
}
