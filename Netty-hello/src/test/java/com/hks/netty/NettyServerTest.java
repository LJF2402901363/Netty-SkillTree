package com.hks.netty;

import com.hks.netty.server.NettyServer;
import org.junit.Test;

/**
 * Classname:NettyServerTest
 *
 * @description:
 * @author: 陌意随影
 * @Date: 2021-05-14 01:06
 * @Version: 1.0
 **/
public class NettyServerTest {
    @Test
    public  void test(){
     int port = 8888;
        try {
            new NettyServer(port).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
