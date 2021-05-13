package client;

import com.moyisuiying.client.EchoClient;

/**
 * Classname:EchoClientTest
 *
 * @description:
 * @author: 陌意随影
 * @Date: 2021-05-13 17:15
 * @Version: 1.0
 **/
public class EchoClientTest {
    public static void main(String[] args) throws Exception {
        new EchoClient("localhost",8888).start();
    }
}
