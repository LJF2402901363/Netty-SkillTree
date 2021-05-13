package com.hks.netty.server;

import java.net.InetAddress;
import java.util.Date;

import com.hks.netty.util.DateUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 
* Title: HelloServerHandler
* Description: 
*  服务端业务逻辑
* Version:1.0.0  
* @author hekuangsheng
* @date 2017-8-31
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
    /*
     * 收到消息时，返回信息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg)
            throws Exception {
        // 收到消息直接打印输出
        if("quit".equals(msg)){//服务端断开的条件
            ctx.close();
        }
        String dateToStr = DateUtil.dateToStr(new Date());
        // 返回客户端消息
        ctx.writeAndFlush(dateToStr+" 收到你的消息啦："+"\n");
    }

    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        ctx.writeAndFlush("客户端"+ InetAddress.getLocalHost().getHostName() + "成功与服务端建立连接！ \n");
        super.channelActive(ctx);
    }
}
