package com.hks.netty.client;

import com.hks.netty.util.DateUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Scanner;

/**
 * 
* Title: NettyClient
* Description: 
* Netty客户端 
* Version:1.0.0  
* @author hekuangsheng
* @date 2017-8-31
 */
public class NettyClient {

    public final String host;  //ip地址
    public final int port ;          //端口
    /// 通过nio方式来接收连接和处理连接   
    private  EventLoopGroup eventLoopGroup = null;
    private   Bootstrap bootstrap = null;
    private  Channel channel;
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
    }

   public void start(){
        this.bootstrap.group(eventLoopGroup);
        //设置Nio方式连接
        this.bootstrap.channel(NioSocketChannel.class);
       //设置过滤器
        this.bootstrap.handler(new NettyClientFilter());
        //连接到远程服务器
       ChannelFuture channelFuture = this.bootstrap.connect(new InetSocketAddress(this.host, this.port));
       try {
           this.channel = channelFuture.sync().channel();
           channel.writeAndFlush(" 客户端发送消息：nihao\r\n");
           channel.closeFuture().sync();
       } catch (Exception e) {
           e.printStackTrace();
       }finally {
           try {
               this.eventLoopGroup.shutdownGracefully().sync();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }


   }


}