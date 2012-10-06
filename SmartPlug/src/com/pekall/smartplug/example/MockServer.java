
package com.pekall.smartplug.example;

import com.pekall.smartplug.codec.SmartPlugDecoder;
import com.pekall.smartplug.codec.SmartPlugEncoder;
import com.pekall.smartplug.message.BaseMessage;
import com.pekall.smartplug.message.GetStatusRequest;
import com.pekall.smartplug.message.HelloResponse;
import com.pekall.smartplug.message.ReportStatusResponse;
import com.pekall.smartplug.message.SetStatusRequest;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class MockServer {
    private static final int SERVER_PORT = 16668;

    private static ChannelGroup sAllChannels;
    private static ChannelFactory sChannelFactory;
    private static int sChannelId;
    
    private static class ServerHandler extends SimpleChannelUpstreamHandler {

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            slog("channelOpen channel id = " + e.getChannel().getId());
            sChannelId = e.getChannel().getId();
            sAllChannels.add(e.getChannel());
            super.channelOpen(ctx, e);
        }
        
        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            // TODO Auto-generated method stub
            slog("channelClosed channel id = " + e.getChannel().getId());
            super.channelClosed(ctx, e);
        }
        
        @Override
        public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            // TODO Auto-generated method stub
            slog("channelBound channel id = " + e.getChannel().getId());
            super.channelBound(ctx, e);
        }
        
        @Override
        public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            // TODO Auto-generated method stub
            slog("channelUnbound channel id = " + e.getChannel().getId());
            super.channelUnbound(ctx, e);
        }
        
        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            // TODO Auto-generated method stub
            slog("channelConnected channel id = " + e.getChannel().getId());
            super.channelConnected(ctx, e);
        }
        
        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            // TODO Auto-generated method stub
            slog("channelDisconnected channel id = " + e.getChannel().getId());
            super.channelDisconnected(ctx, e);
        }
        
        @Override
        public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
            slog("childChannelOpen child channel id = " + e.getChildChannel().getId());
            super.childChannelOpen(ctx, e);
        }
        
        @Override
        public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
            slog("childChannelClosed child channel id = " + e.getChildChannel().getId());
            super.childChannelClosed(ctx, e);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            BaseMessage message = (BaseMessage) e.getMessage();

            slog(message.toString());

            switch (message.getMessageType()) {
                case MSG_HELLO_REQ:
                    sendHelloResponse(message, e.getChannel());
                    break;
                case MSG_REPORT_STATUS_REQ:
                    sendReportStatusResponse(message, e.getChannel());
                    break;

                default:
                    break;
            }
        }

        private void sendReportStatusResponse(BaseMessage request, Channel channel) {
            slog("sendReportStatusResponse E");
            ReportStatusResponse response = new ReportStatusResponse(request.getMessageId());
            channel.write(response);
            slog("sendReportStatusResponse X");
        }

        private void sendHelloResponse(BaseMessage request, Channel channel) {
            slog("sendHelloResponse E");
            HelloResponse response = new HelloResponse(request.getMessageId(), (short) 0, "demo smart plug server");
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

        slog("server start channel id = " + channel.getId() + " class = " + channel.getClass().toString());
    }

    private static void stop() {
        ChannelGroupFuture future = sAllChannels.close();
        future.awaitUninterruptibly();
        sChannelFactory.releaseExternalResources();
        slog("server stop");
    }

    private static void sendSetStatusReq(boolean status) {
        Channel channel = sAllChannels.find(sChannelId);
        SetStatusRequest request = new SetStatusRequest(0, (short)(status ? 1 : 0));
        channel.write(request);
    }

    private static void sendGetStatusReq() {
        Channel channel = sAllChannels.find(sChannelId);
        GetStatusRequest request = new GetStatusRequest(0);
        channel.write(request);
    }

    private static void printConsole() {
        System.out.println("----- MOCK SERVER CONSOLE -----");
        System.out.println("1. send set status on");
        System.out.println("2. send set status off");
        System.out.println("3. send get status");
        System.out.println("0. stop server");
        System.out.println("-------------------------------");
    }

    public static void main(String[] args) {
        start();

        Scanner scanner = new Scanner(System.in);

        boolean exit = false;

        while (!exit) {
            printConsole();
            
            int option = scanner.nextInt(); // wait for user input
            switch (option) {
                case 0:
                    stop();
                    exit = true;
                    break;
                case 1:
                    sendSetStatusReq(true);
                    break;
                case 2:
                    sendSetStatusReq(false);
                    break;
                case 3:
                    sendGetStatusReq();
                    break;

                default:
                    break;
            }

        }
    }
}
