package com.moyisuiying.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Classname:EchoServerHandler
 *
 * @description:
 * @author: 陌意随影
 * @Date: 2021-05-13 16:12
 * @Version: 1.0
 **/
//实例之间可以在 channel 里面共享
@ChannelHandler.Sharable
public class EchoServerHandler  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        //日志消息输出到控制台
        System.out.println("server received " + buf.toString(CharsetUtil.UTF_8));
        //将所接收的消息返回给发送者。注意，这还没有冲刷数据
        ctx.write(buf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //冲刷所有待审消息到远程节点。关闭通道后，操作完成
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
        .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //.打印异常堆栈跟踪
        cause.printStackTrace();
        //关闭通道
        ctx.close();
    }
}
