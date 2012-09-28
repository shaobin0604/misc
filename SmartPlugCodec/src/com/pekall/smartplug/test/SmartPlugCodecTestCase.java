
package com.pekall.smartplug.test;

import com.pekall.smartplug.codec.SmartPlugDecoder;
import com.pekall.smartplug.codec.SmartPlugEncoder;
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

import junit.framework.JUnit4TestAdapter;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class SmartPlugCodecTestCase {
    private static final int SERVER_PORT = 8888;

    private static ChannelGroup sAllChannels;
    private static ChannelFactory sChannelFactory;
    
    private ClientBootstrap mClientBootstrap;
    private Channel mClientChannel;
    
    private static void slog(String msg) {
        System.out.println("server --> " + msg);
    }
    
    private static void clog(String msg) {
        System.out.println("client --> " + msg);
    }

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
                case MSG_REPORT_STATUS_REQ:
                    sendReportStatusResponse(message, e.getChannel());
                    break;
                case MSG_GET_STATUS_REQ:
                    sendGetStatusResponse(message, e.getChannel());
                    break;
                case MSG_SET_STATUS_REQ:
                    sendSetStatusResponse(message, e.getChannel());
                    break;

                default:
                    break;
            }
        }
        
        private void sendSetStatusResponse(BaseMessage message, Channel channel) {
            SetStatusResponse response = new SetStatusResponse(message.getMessageId(), (short)0);
            channel.write(response);
        }

        private void sendGetStatusResponse(BaseMessage message, Channel channel) {
            GetStatusResponse response = new GetStatusResponse(message.getMessageId(), (short)0);
            channel.write(response);
        }

        private void sendReportStatusResponse(BaseMessage message, Channel channel) {
            ReportStatusResponse response = new ReportStatusResponse(message.getMessageId());
            channel.write(response);
        }

        private void sendHelloResponse(BaseMessage request, Channel channel) {
            HelloResponse response = new HelloResponse(request.getMessageId(), (short)0, "demo smart plug server");
            channel.write(response);
        }
    }

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
                    response = answer.take();
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
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            channel = e.getChannel();
            super.channelOpen(ctx, e);
        }
        
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            BaseMessage message = (BaseMessage) e.getMessage();
            boolean offered = answer.offer(message);
            assert offered;
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            System.out.println("exceptionCaught" + e);
            e.getChannel().close();
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.out.println("BeforeClass");
        
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
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        System.out.println("AfterClass");
        ChannelGroupFuture future = sAllChannels.close();
        future.awaitUninterruptibly();
        sChannelFactory.releaseExternalResources();
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("Before");
        // Set up.
        mClientBootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Configure the event pipeline factory.
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

        // Make a new connection.
        ChannelFuture connectFuture =
                mClientBootstrap.connect(new InetSocketAddress("localhost", SERVER_PORT));

        // Wait until the connection is made successfully.
        mClientChannel = connectFuture.awaitUninterruptibly().getChannel();
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("After");
        // Close the connection.
        mClientChannel.close().awaitUninterruptibly();

        // Shut down all thread pools to exit.
        mClientBootstrap.releaseExternalResources();
    }

    @Test
    public void testHello() {
        System.out.println("testHello");
        // Get the handler instance to initiate the request.
        ClientHandler handler = mClientChannel.getPipeline().get(ClientHandler.class);
        
        int messageId = 0;
        String deviceId = "123456789";
        String deviceMode = "abcdefghi";
        
        HelloRequest request = new HelloRequest(messageId, deviceId, deviceMode);
        
        // Request and get the response.
        BaseMessage response = handler.getResponse(request);
        
        clog(response.toString());
        
        Assert.assertEquals(messageId, response.getMessageId());
    }
    
    @Test
    public void testReportStatus() {
        System.out.println("testReportStatus");
        
        // Get the handler instance to initiate the request.
        ClientHandler handler = mClientChannel.getPipeline().get(ClientHandler.class);
        
        int messageId = 0;
        short status = 0;
        
        ReportStatusRequest request = new ReportStatusRequest(messageId, status);
        
        // Request and get the response.
        BaseMessage response = handler.getResponse(request);
        
        clog(response.toString());
    }
    
    @Test
    public void testGetStatus() {
        System.out.println("testGetStatus");
        
        // Get the handler instance to initiate the request.
        ClientHandler handler = mClientChannel.getPipeline().get(ClientHandler.class);
        
        int messageId = 0;
        
        GetStatusRequest request = new GetStatusRequest(messageId);
        
        // Request and get the response.
        BaseMessage response = handler.getResponse(request);
        
        clog(response.toString());
    }
    
    @Test
    public void testSetStatus() {
        System.out.println("testSetStatus");
        
        // Get the handler instance to initiate the request.
        ClientHandler handler = mClientChannel.getPipeline().get(ClientHandler.class);
        
        int messageId = 0;
        short status = 1;
        
        SetStatusRequest request = new SetStatusRequest(messageId, status);
        
        // Request and get the response.
        BaseMessage response = handler.getResponse(request);
        
        clog(response.toString());
    }
    
    @Test
    public void testHeartbeat() {
        System.out.println("testHeartbeat");
        
        int messageId = 0;
        short status = 1;
        
        Heartbeat heartbeat = new Heartbeat(messageId, status);
        
        mClientChannel.write(heartbeat);
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(SmartPlugCodecTestCase.class);
    }
}
