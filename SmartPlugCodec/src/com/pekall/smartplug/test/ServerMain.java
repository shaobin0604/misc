package com.pekall.smartplug.test;

import com.pekall.smartplug.codec.SmartPlugDecoder;
import com.pekall.smartplug.codec.SmartPlugEncoder;
import com.pekall.smartplug.message.BaseMessage;
import com.pekall.smartplug.message.HelloResponse;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServerMain {
    private static final int SERVER_PORT = 8888;

    private static ChannelGroup sAllChannels;
    private static ChannelFactory sChannelFactory;

    private static class ServerHandler extends SimpleChannelUpstreamHandler {

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            sAllChannels.add(e.getChannel());
            super.channelOpen(ctx, e);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            BaseMessage message = (BaseMessage) e.getMessage();
            
            slog(message.toString());
            
            switch (message.getMessageType()) {
                case MSG_HELLO_REQ:
                    sendHelloResponse(message, e.getChannel());
                    break;

                default:
                    break;
            }
        }
        
        private void sendHelloResponse(BaseMessage request, Channel channel) {
            slog("sendHelloResponse E");
            HelloResponse response = new HelloResponse(request.getMessageId(), (short)0, "demo smart plug server");
            channel.write(response);
            slog("sendHelloResponse X");
        }
    }
    
    private static void slog(String msg) {
        System.out.println("server --> " + msg);
    }
    
    private static void start() {
        sAllChannels = new DefaultChannelGroup("server channels");
        sChannelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(sChannelFactory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new SmartPlugDecoder());
                pipeline.addLast("encoder", new SmartPlugEncoder());
                pipeline.addLast("handler", new ServerHandler());

                return pipeline;
            }
        });

        Channel channel = bootstrap.bind(new InetSocketAddress(SERVER_PORT));
        sAllChannels.add(channel);
        
        slog("server start");
    }
    
    private static void stop() {
        ChannelGroupFuture future = sAllChannels.close();
        future.awaitUninterruptibly();
        sChannelFactory.releaseExternalResources();
    }
    
    public static void main(String[] args) {
        start();
    }
}
