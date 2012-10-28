
package com.pekall.pctool;

import android.content.Context;

import com.pekall.pctool.model.HandlerFacade;
import com.pekall.pctool.util.Slog;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final int SERVER_PORT = 12580;

    private Context mContext;
    private ChannelGroup mAllChannels;
    private ChannelFactory mFactory;
    private volatile boolean mIsAlive;

    public HttpServer(Context context) {
        super();
        mContext = context;
    }

    private static class ServerPipelineFactory implements
            ChannelPipelineFactory {
        private Context mContext;
        
        public ServerPipelineFactory(Context context) {
            this.mContext = context;
        }
        
        public ChannelPipeline getPipeline() throws Exception {
            // Create a default pipeline implementation.
            ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("encoder", new HttpResponseEncoder());

            pipeline.addLast("handler", new HttpServerHandler(new HandlerFacade(mContext)));
            return pipeline;
        }
    }
    
    public boolean isAlive() {
       return mIsAlive;
    }

    public synchronized void start() {
        Slog.d("start E, port = " + SERVER_PORT);

        mAllChannels = new DefaultChannelGroup("main-server");
        
        mFactory = new OioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
//      mFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        
        ServerBootstrap bootstrap = new ServerBootstrap(mFactory);
        bootstrap.setPipelineFactory(new ServerPipelineFactory(mContext));
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
}
