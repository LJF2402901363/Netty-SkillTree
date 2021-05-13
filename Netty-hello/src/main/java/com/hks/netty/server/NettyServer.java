package com.hks.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * 
* Title: NettyServer
* Description: 
*  Netty服务端
* Version:1.0.0  
* @author hekuangsheng
* @date 2017-8-31
 */
public class NettyServer {

        private  final int port; //设置服务端端口
        private   EventLoopGroup eventLoopGroup = null;   // 通过nio方式来接收连接和处理连接
        private   ServerBootstrap serverBootstrap = null;

    public NettyServer(int port) {
        this.port = port;
        this.serverBootstrap = new ServerBootstrap();
        this.eventLoopGroup = new NioEventLoopGroup();
    }

    public void start() throws InterruptedException {
        try {
            this.serverBootstrap.group(eventLoopGroup);
            //采用Nio方式
            serverBootstrap.channel(NioServerSocketChannel.class);
            //设置绑定端口
            serverBootstrap.localAddress(new InetSocketAddress(this.port));
            //设置过滤器
            serverBootstrap.childHandler(new NettyServerFilter());
            //获取
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("服务端启动，正在监听等待客户端连接。。。。");
            channelFuture.channel().closeFuture().sync();
        } finally {
            //优雅关闭事件处理线程池
            this.eventLoopGroup.shutdownGracefully();
        }

    }
}
