package com.moyisuiying.server;

import com.moyisuiying.handler.EchoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Classname:EchoServer
 *
 * @description:
 * @author: 陌意随影
 * @Date: 2021-05-13 16:26
 * @Version: 1.0
 **/

public class EchoServer {
    //端口
    private final   int port;
    public EchoServer(int port) {
        this.port = port;
    }
    public void start() throws  Exception {
        //创建 EventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            //创建 ServerBootstrap
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
            .channel(NioServerSocketChannel.class)//指定使用 NIO 的传输 Channel
            .localAddress(new InetSocketAddress(port))//设置 socket 地址使用所选的端口
            .childHandler(new ChannelInitializer<SocketChannel>() { //添加 EchoServerHandler 到 Channel 的 ChannelPipeline
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                  socketChannel.pipeline().addLast(new EchoServerHandler());
                }
            })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            ChannelFuture channelFuture = bootstrap.bind().sync();//绑定的服务器;sync 等待服务器关闭
            System.out.println(EchoServer.class.getName() + "started and listen on " + channelFuture.channel().localAddress());
            channelFuture.channel().closeFuture().sync();//关闭 channel 和 块，直到它被关闭
        }  finally {
            group.shutdownGracefully().sync(); //关机的 EventLoopGroup，释放所有资源。
        }
    }
}
