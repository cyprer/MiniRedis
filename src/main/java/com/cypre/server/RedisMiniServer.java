package com.cypre.server;

import com.cypre.server.core.RedisCore;
import com.cypre.server.core.RedisCoreImpl;
import com.cypre.server.handler.RespCommandHandler;
import com.cypre.server.handler.RespDecoder;
import com.cypre.server.handler.RespEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RedisMiniServer implements RedisServer{

    private static final int DEFAULT_DBCOUNT = 16;

    private String host;
    private int port;
    private RedisCore redisCore;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public RedisMiniServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(4);
        this.redisCore = new RedisCoreImpl(DEFAULT_DBCOUNT);
    }


    @Override
    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RespDecoder());
                        pipeline.addLast(new RespCommandHandler(redisCore));
                        pipeline.addLast(new RespEncoder());
                    }
                });
        try{
            serverChannel = serverBootstrap.bind(host, port).sync().channel();
            System.out.println("Redis server started at " + host + ":" + port);
        }
        catch (InterruptedException e){
            e.printStackTrace();
            stop();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        if(bossGroup != null){
            bossGroup.shutdownGracefully();
        }
        if(workerGroup != null){
            workerGroup.shutdownGracefully();
        }
        if(serverChannel != null){
            serverChannel.close();
        }
    }
}
