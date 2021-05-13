package com.moyisuiying.client;

import com.moyisuiying.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Classname:EchoClient
 *
 * @description:
 * @author: 陌意随影
 * @Date: 2021-05-13 16:39
 * @Version: 1.0
 **/
public class EchoClient {
    private  final String host;
    private final Integer port;

    public EchoClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }
    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap(); //创建 Bootstrap
            bootstrap.group(group) //指定 EventLoopGroup 来处理客户端事件。由于我们使用 NIO 传输，所以用到了 NioEventLoopGroup 的实现
            .channel(NioSocketChannel.class)//使用的 channel 类型是一个用于 NIO 传输
            .remoteAddress(new InetSocketAddress(host, port)) //设置服务器的 InetSocketAddress
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel nioServerSocketChannel) throws Exception {
                    nioServerSocketChannel.pipeline().addLast(new ClientHandler());//当建立一个连接和一个新的通道时，创建添加到 EchoClientHandler 实例 到 channel pipeline
                }
            });
            ChannelFuture channelFuture = bootstrap.connect().sync(); //连接到远程;等待连接完成
            channelFuture.channel().closeFuture().sync(); //阻塞直到 Channel 关闭

        }finally {
           group.shutdownGracefully().sync(); //调用 shutdownGracefully() 来关闭线程池和释放所有资源
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2){
            System.err.println("Usage:"+EchoClient.class.getSimpleName()+"<host> <port>");
            return;
        }
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        new  EchoClient(host,port).start();
    }
}
