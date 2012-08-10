package com.pekall.pctool;


import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class MainServer {
    private static final int SERVER_PORT = 12580;

    private ChannelGroup mAllChannels;
    private ChannelFactory mFactory;
    private volatile boolean mIsAlive;

    private static class ServerPipelineFactory implements
            ChannelPipelineFactory {
                
        public ChannelPipeline getPipeline() throws Exception {
            // Create a default pipeline implementation.
            ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("encoder", new HttpResponseEncoder());
            // http处理handler
            pipeline.addLast("handler", new MainServerHandler(new FakeBusinessLogicFacade()));
            return pipeline;
        }
    }
    
    public boolean isAlive() {
       return mIsAlive;
    }

    public synchronized void start() {
        Slog.d("start E, port = " + SERVER_PORT);

        mAllChannels = new DefaultChannelGroup("main-server");
        mFactory = new OioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        // 配置服务器-使用java线程池作为解释线程
        ServerBootstrap bootstrap = new ServerBootstrap(mFactory);
        // 设置 pipeline factory.
        bootstrap.setPipelineFactory(new ServerPipelineFactory());
        // 绑定端口
        Channel channel = bootstrap.bind(new InetSocketAddress(SERVER_PORT));
        mAllChannels.add(channel);
        
        mIsAlive = true;
        Slog.d("start X");
    }

    public synchronized void stop() {
        Slog.d("stop E");
        ChannelGroupFuture future = mAllChannels.close();
        future.awaitUninterruptibly();
        mFactory.releaseExternalResources();
        Slog.d("stop X");
    }
    
    public static void main(String[] args) {
        new MainServer().start();
    }
}
