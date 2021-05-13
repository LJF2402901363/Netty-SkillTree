package server;

import com.moyisuiying.server.EchoServer;

/**
 * Classname:EchoServer
 *
 * @description:
 * @author: 陌意随影
 * @Date: 2021-05-13 17:17
 * @Version: 1.0
 **/
public class EchoServerTest {
    public static void main(String[] args) throws Exception {
        new EchoServer(8888).start();
    }
}
