package com.hks.netty;

import com.hks.netty.client.NettyClient;
import org.junit.Test;

/**
 * Classname:NettyClientTest
 *
 * @description:
 * @author: 陌意随影
 * @Date: 2021-05-14 01:08
 * @Version: 1.0
 **/
public class NettyClientTest {
    @Test
    public  void testClient(){
        String host = "localhost";
        int port = 8888;
        new NettyClient(host, port).start();
    }
}
