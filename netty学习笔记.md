# netty 学习笔记

## 1.netty简介

### 1.1什么是netty

> Netty是由[JBOSS](https://baike.baidu.com/item/JBOSS)提供的一个[java开源](https://baike.baidu.com/item/java开源/10795649)框架，现为 [Github](https://baike.baidu.com/item/Github/10145341)上的独立项目。Netty提供异步的、[事件驱动](https://baike.baidu.com/item/事件驱动/9597519)的网络应用程序框架和工具，用以快速开发高性能、高可靠性的[网络服务器](https://baike.baidu.com/item/网络服务器/99096)和客户端程序。
>
> 也就是说，Netty 是一个基于NIO的客户、服务器端的编程框架，使用Netty 可以确保你快速和简单的开发出一个网络应用，例如实现了某种协议的客户、[服务端](https://baike.baidu.com/item/服务端/6492316)应用。Netty相当于简化和流线化了网络应用的编程开发过程，例如：基于TCP和UDP的socket服务开发。
>
> “快速”和“简单”并不用产生维护性或性能上的问题。Netty 是一个吸收了多种协议（包括FTP、SMTP、HTTP等各种二进制文本协议）的实现经验，并经过相当精心设计的项目。最终，Netty 成功的找到了一种方式，在保证易于开发的同时还保证了其应用的性能，稳定性和伸缩性。 



### 1.2netty出现的历史

#### 1.2.1BIO的出现

Java 早期版本(1995-2002)介绍了足够的面向对象的糖衣来隐藏一些复杂性，但实现复杂的客户端-服务器协议仍然需要大量的样板代码（和进行大量的监视才能确保他们是对的）。

![image-20210513113626127](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513113626127.png)

这些早期的 Java API（java.net）只能通过原生的 socket 库来支持所谓的“blocking（阻塞）”的功能。一个简单的例子

```java
ServerSocket serverSocket = new ServerSocket(portNumber);//1
Socket clientSocket = serverSocket.accept();             //2
BufferedReader in = new BufferedReader(                     //3
        new InputStreamReader(clientSocket.getInputStream()));
PrintWriter out =
        new PrintWriter(clientSocket.getOutputStream(), true);
String request, response;
while ((request = in.readLine()) != null) {                 //4
    if ("Done".equals(request)) {                         //5
        break;
    }
}
response = processRequest(request);                        //6
out.println(response);                                    //7
                                                        //8
```

具体的流程为：

> 1.ServerSocket 创建并监听端口的连接请求
>
> 2.accept() 调用阻塞，直到一个连接被建立了。返回一个新的 Socket 用来处理 客户端和服务端的交互
>
> 3.流被创建用于处理 socket 的输入和输出数据。BufferedReader 读取从字符输入流里面的本文。PrintWriter 打印格式化展示的对象读到本文输出流
>
> 4.处理循环开始 readLine() 阻塞，读取字符串直到最后是换行或者输入终止。
>
> 5.如果客户端发送的是“Done”处理循环退出
>
> 6.执行方法处理请求，返回服务器的响应
>
> 7.响应发回客户端
>
> 8.处理循环继续

显然，这段代码限制每次只能处理一个连接。为了实现多个并行的客户端我们需要分配一个新的 Thread 给每个新的客户端 Socket(当然需要更多的代码)。但考虑使用这种方法来支持大量的同步，长连接。在任何时间点多线程可能处于休眠状态，等待输入或输出数据。这很容易使得资源的大量浪费，对性能产生负面影响。

#### 1.2.2JAVA NIO

在 2002 年，Java 1.4 引入了非阻塞 API 在 java.nio 包（NIO）。

*"New"还是"Nonblocking"?*

*NIO 最初是为 New Input/Output 的缩写。然而，Java 的 API 已经存在足够长的时间，它不再是新的。现在普遍使用的缩写来表示Nonblocking I/O (非阻塞 I/O)。另一方面，一般（包括作者）指阻塞 I/O 为 OIO 或 Old Input/Output。你也可能会遇到普通 I/O。*

非阻塞I/O，主要是消除了这些方法 约束。在这里，我们介绍了“Selector”，这是 Java 的无阻塞 I/O 实现的关键。

![image-20210513144029317](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513144029317.png)

Selector 最终决定哪一组注册的 socket 准备执行 I/O。正如我们之前所解释的那样，这 I/O 操作设置为非阻塞模式。通过通知，一个线程可以同时处理多个并发连接。（一个 Selector 由一个线程通常处理，但具体实施可以使用多个线程。）因此，每次读或写操作执行能立即检查完成。总体而言，该模型提供了比 阻塞 I/O 模型 更好的资源使用，因为

- 可以用较少的线程处理更多连接，这意味着更少的开销在内存和上下文切换上
- 当没有 I/O 处理时，线程可以被重定向到其他任务上。

你可以直接用这些 Java API 构建的 NIO 建立你的应用程序，但这样做 正确和安全是无法保证的。实现可靠和可扩展的 event-processing（事件处理器）来处理和调度数据并保证尽可能有效地，这是一个繁琐和容易出错的任务，最好留给专家 - Netty。

### 1.3 Netty的作用

Netty 是一个广泛使用的 Java 网络编程框架(Netty 在 2011 年获得了Duke's Choice Award，见https://www.java.net/dukeschoice/2011。它的活跃和成长用户社区包括大型公司像 Facebook 和 Instagram 以及流行 开源项目如 Infinispan, HornetQ, Vert.x, Apache Cassandra 和 Elasticsearch，都利用其强大的网络抽象的核心代码。

反过来，Netty 也从这些项目中获益。随着这些项目的作用，也提高其范围和灵活性，比如实现的实现的协议 FTP, SMTP, HTTP, WebSocket 和 SPDY 以及其他二进制和基于文本的。

在初创公司中 Firebase 和 Urban Airship 在使用 Netty。前者 Firebase 是使用 long-lived HTTP 连接，后者是使用 各种推送通知。

当你使用 Twitter,你会使用 Finagle,这个是基于 Netty API 提供给内部系统通讯。Facebook 使用 Netty 来提供于 Nifty 类似的功能 Apache Thrift 服务。这些公司可扩展性和高性能的表现得益于 Netty 的贡献。

这些例子的真实案例会在后面几章讲到。

2011 年 Netty 项目从 Red Hat 独立开来从而让广泛的开发者社区贡献者参与进来。Red Hat ，Twitter 继续使用 Netty ,并且成为保持其最活跃的贡献者之一。

下面展示了 Netty 技术和方法的特点

- 设计
  - 针对多种传输类型的统一接口 - 阻塞和非阻塞
  - 简单但更强大的线程模型
  - 真正的无连接的数据报套接字支持
  - 链接逻辑支持复用
- 易用性
  - 大量的 Javadoc 和 代码实例
  - 除了在 JDK 1.6 + 额外的限制。（一些特征是只支持在Java 1.7 +。可选的功能可能有额外的限制。）
- 性能
  - 比核心 Java API 更好的吞吐量，较低的延时
  - 资源消耗更少，这个得益于共享池和重用
  - 减少内存拷贝
- 健壮性
  - 消除由于慢，快，或重载连接产生的 OutOfMemoryError
  - 消除经常发现在 NIO 在高速网络中的应用中的不公平的读/写比
- 安全
  - 完整的 SSL / TLS 和 StartTLS 的支持
  - 运行在受限的环境例如 Applet 或 OSGI
- 社区
  - 发布的更早和更频繁
  - 社区驱动

### 1.4netty的核心构成

![image-20210513144455964](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513144455964.png)

#### 1.4.1Channel

[Channel](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/Channel.html) 是 NIO 基本的结构。它代表了一个用于连接到实体如硬件设备、文件、网络套接字或程序组件,能够执行一个或多个不同的 I/O 操作（例如读或写）的开放连接。

现在,把 Channel 想象成一个可以“打开”或“关闭”,“连接”或“断开”和作为传入和传出数据的运输工具。

#### 1.4.2Callback (回调)

callback (回调)是一个简单的方法,提供给另一种方法作为引用,这样后者就可以在某个合适的时间调用前者。这种技术被广泛使用在各种编程的情况下,最常见的方法之一通知给其他人操作已完成。

Netty 内部使用回调处理事件时。一旦这样的回调被触发，事件可以由接口 ChannelHandler 的实现来处理。如下面的代码，一旦一个新的连接建立了,调用 channelActive(),并将打印一条消息。

```java
public class ConnectHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {   //1
        System.out.println(
                "Client " + ctx.channel().remoteAddress() + " connected");
    }
}
```

#### 1.4.3Future

Future 提供了另外一种通知应用操作已经完成的方式。这个对象作为一个异步操作结果的占位符,它将在将来的某个时候完成并提供结果。

JDK 附带接口 java.util.concurrent.Future ,但所提供的实现只允许您手动检查操作是否完成或阻塞了。这是很麻烦的，所以 Netty 提供自己了的实现,ChannelFuture,用于在执行异步操作时使用。

ChannelFuture 提供多个附件方法来允许一个或者多个 ChannelFutureListener 实例。这个回调方法 operationComplete() 会在操作完成时调用。事件监听者能够确认这个操作是否成功或者是错误。如果是后者,我们可以检索到产生的 Throwable。简而言之, ChannelFutureListener 提供的通知机制不需要手动检查操作是否完成的。

每个 Netty 的 outbound I/O 操作都会返回一个 ChannelFuture;这样就不会阻塞。这就是 Netty 所谓的“自底向上的异步和事件驱动”。

下面例子简单的演示了作为 I/O 操作的一部分 ChannelFuture 的返回。当调用 connect() 将会直接是非阻塞的，并且调用在背后完成。由于线程是非阻塞的，所以无需等待操作完成，而可以去干其他事，因此这令资源利用更高效。

##### 1.4.3.1回调使用例子

```java
Channel channel = ...;
//不会阻塞
ChannelFuture future = channel.connect(
    new InetSocketAddress("192.168.0.1", 25));
```

##### 1.4.3.2异步连接到远程地址

下面代码描述了如何利用 ChannelFutureListener 。首先，连接到远程地址。接着，通过 ChannelFuture 调用 connect() 来 注册一个新ChannelFutureListener。当监听器被通知连接完成，我们检查状态。如果是成功，就写数据到 Channel，否则我们检索 ChannelFuture 中的Throwable。

注意，错误的处理取决于你的项目。当然,特定的错误是需要加以约束 的。例如,在连接失败的情况下你可以尝试连接到另一个。

```java
Channel channel = ...;
//不会阻塞
ChannelFuture future = channel.connect(            //1
        new InetSocketAddress("192.168.0.1", 25));
future.addListener(new ChannelFutureListener() {  //2
@Override
public void operationComplete(ChannelFuture future) {
    if (future.isSuccess()) {                    //3
        ByteBuf buffer = Unpooled.copiedBuffer(
                "Hello", Charset.defaultCharset()); //4
        ChannelFuture wf = future.channel().writeAndFlush(buffer);                //5
        // ...
    } else {
        Throwable cause = future.cause();        //6
        cause.printStackTrace();
    }
}
});
```

1.异步连接到远程对等。调用立即返回并提供 ChannelFuture。

2.操作完成后通知注册一个 ChannelFutureListener 。

3.当 operationComplete() 调用时检查操作的状态。

4.如果成功就创建一个 ByteBuf 来保存数据。

5.异步发送数据到远程。再次返回ChannelFuture。

6.如果有一个错误则抛出 Throwable,描述错误原因

#### 1.4.4Event 和 Handler

Netty 使用不同的事件来通知我们更改的状态或操作的状态。这使我们能够根据发生的事件触发适当的行为。

这些行为可能包括：

- 日志
- 数据转换
- 流控制
- 应用程序逻辑

由于 Netty 是一个网络框架,事件很清晰的跟入站或出站数据流相关。因为一些事件可能触发传入的数据或状态的变化包括:

- 活动或非活动连接
- 数据的读取
- 用户事件
- 错误

出站事件是由于在未来操作将触发一个动作。这些包括:

- 打开或关闭一个连接到远程
- 写或冲刷数据到 socket

每个事件都可以分配给用户实现处理程序类的方法。这说明了事件驱动的范例可直接转换为应用程序构建块。

图中显示了一个事件可以由一连串的事件处理器来处理：

![image-20210513145529475](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513145529475.png)

Netty 还提供了一组丰富的预定义的处理程序,您可以开箱即用。这些是各种协议的编解码器包括 HTTP 和 SSL/TLS。在内部,ChannelHandler 使用事件和 future 本身,使得消费者的具有 Netty 的抽象

#### 1.4.5整合

##### 1.4.5.1FUTURE, CALLBACK 和 HANDLER

Netty 的异步编程模型是建立在 future 和 callback 的概念上的。所有这些元素的协同为自己的设计提供了强大的力量。

拦截操作和转换入站或出站数据只需要您提供回调或利用 future 操作返回的。这使得链操作简单、高效,促进编写可重用的、通用的代码。一个 Netty 的设计的主要目标是促进“关注点分离”:你的业务逻辑从网络基础设施应用程序中分离。

##### 1.4.5.2 SELECTOR, EVENT 和 EVENT LOOP

Netty 通过触发事件从应用程序中抽象出 Selector，从而避免手写调度代码。EventLoop 分配给每个 Channel 来处理所有的事件，包括

- 注册有趣的事件
- 调度事件到 ChannelHandler
- 安排进一步行动

该 EventLoop 本身是由只有一个线程驱动，它给一个 Channel 处理所有的 I/O 事件，并且在 EventLoop 的生命周期内不会改变。这个简单而强大的线程模型消除你可能对你的 ChannelHandler 同步的任何关注，这样你就

## 2.netty的入门使用

### 2.1安装配置环境

 jdk1.8+,idea,maven

### 2.2Netty 客户端/服务器 总览

![image-20210513150759279](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513150759279.png)

图中显示了连接到服务器的多个并发的客户端。在理论上，客户端可以支持的连接数只受限于使用的 JDK 版本中的制约。

echo（回声）客户端和服务器之间的交互是很简单的;客户端后，建立一个连接发送一个或多个消息发送到服务器，其中每相呼应消息返回给客户端。诚然，这个应用程序并不是非常有用。但这项工作是为了更好的理解请求 - 响应交互本身，这是一个基本的模式的客户端/服务器系统。

我们将通过检查服务器端代码开始。

### 2.3写一个 echo 服务器

所以 Netty 服务器都需要下面这些：

- 一个服务器 handler：这个组件实现了服务器的业务逻辑，决定了连接创建后和接收到信息后该如何处理
- Bootstrapping： 这个是配置服务器的启动代码。最少需要设置服务器绑定的端口，用来监听连接请求。

#### 2.3.1通过 ChannelHandler 来实现服务器的逻辑

Echo Server 将会将接受到的数据的拷贝发送给客户端。因此，我们需要实现 ChannelInboundHandler 接口，用来定义处理入站事件的方法。由于我们的应用很简单，只需要继承 ChannelInboundHandlerAdapter 就行了。这个类 提供了默认 ChannelInboundHandler 的实现，所以只需要覆盖下面的方法：

- channelRead() - 每个信息入站都会调用
- channelReadComplete() - 通知处理器最后的 channelread() 是当前批处理中的最后一条消息时调用
- exceptionCaught()- 读操作时捕获到异常时调用

EchoServerHandler 代码如下：

```java
@Sharable                                        //1
public class EchoServerHandler extends
        ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx,
        Object msg) {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Server received: " + in.toString(CharsetUtil.UTF_8));        //2
        ctx.write(in);                            //3
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)//4
        .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
        Throwable cause) {
        cause.printStackTrace();                //5
        ctx.close();                            //6
    }
}
```

> 1.`@Sharable` 标识这类的实例之间可以在 channel 里面共享
>
> 2.日志消息输出到控制台
>
> 3.将所接收的消息返回给发送者。注意，这还没有冲刷数据
>
> 4.冲刷所有待审消息到远程节点。关闭通道后，操作完成
>
> 5.打印异常堆栈跟踪
>
> 6.关闭通道

这种使用 ChannelHandler 的方式体现了关注点分离的设计原则，并简化业务逻辑的迭代开发的要求。处理程序很简单;它的每一个方法可以覆盖到“hook（钩子）”在活动周期适当的点。很显然，我们覆盖 channelRead因为我们需要处理所有接收到的数据。

覆盖 exceptionCaught 使我们能够应对任何 Throwable 的子类型。在这种情况下我们记录，并关闭所有可能处于未知状态的连接。它通常是难以 从连接错误中恢复，所以干脆关闭远程连接。当然，也有可能的情况是可以从错误中恢复的，所以可以用一个更复杂的措施来尝试识别和处理 这样的情况。

*如果异常没有被捕获，会发生什么？*

*每个 Channel 都有一个关联的 ChannelPipeline，它代表了 ChannelHandler 实例的链。适配器处理的实现只是将一个处理方法调用转发到链中的下一个处理器。因此，如果一个 Netty 应用程序不覆盖exceptionCaught ，那么这些错误将最终到达 ChannelPipeline，并且结束警告将被记录。出于这个原因，你应该提供至少一个 实现 exceptionCaught 的 ChannelHandler。*

关键点要牢记：

- ChannelHandler 是给不同类型的事件调用
- 应用程序实现或扩展 ChannelHandler 挂接到事件生命周期和 提供自定义应用逻辑。

#### 2.3.2引导服务器

了解到业务核心处理逻辑 EchoServerHandler 后，下面要引导服务器自身了。

- 监听和接收进来的连接请求
- 配置 Channel 来通知一个关于入站消息的 EchoServerHandler 实例

*Transport(传输）*

*在本节中，你会遇到“transport(传输）”一词。在网络的多层视图协议里面，传输层提供了用于端至端或主机到主机的通信服务。互联网通信的基础是 TCP 传输。当我们使用术语“NIO transport”我们指的是一个传输的实现，它是大多等同于 TCP ，除了一些由 Java NIO 的实现提供了服务器端的性能增强。Transport 详细在第4章中讨论。*

Listing 2.3 EchoServer

```java
public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }
        public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(
                    "Usage: " + EchoServer.class.getSimpleName() +
                    " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);        //1
        new EchoServer(port).start();                //2
    }

    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(); //3
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)                                //4
             .channel(NioServerSocketChannel.class)        //5
             .localAddress(new InetSocketAddress(port))    //6
             .childHandler(new ChannelInitializer<SocketChannel>() { //7
                 @Override
                 public void initChannel(SocketChannel ch) 
                     throws Exception {
                     ch.pipeline().addLast(
                             new EchoServerHandler());
                 }
             });

            ChannelFuture f = b.bind().sync();            //8
            System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();            //9
        } finally {
            group.shutdownGracefully().sync();            //10
        }
    }

}
```

> 1.设置端口值（抛出一个 NumberFormatException 如果该端口参数的格式不正确）
>
> 2.呼叫服务器的 start() 方法
>
> 3.创建 EventLoopGroup
>
> 4.创建 ServerBootstrap
>
> 5.指定使用 NIO 的传输 Channel
>
> 6.设置 socket 地址使用所选的端口
>
> 7.添加 EchoServerHandler 到 Channel 的 ChannelPipeline
>
> 8.绑定的服务器;sync 等待服务器关闭
>
> 9.关闭 channel 和 块，直到它被关闭
>
> 10.关机的 EventLoopGroup，释放所有资源。

在这个例子中，代码创建 ServerBootstrap 实例（步骤4）。由于我们使用在 NIO 传输，我们已指定 NioEventLoopGroup（3）接受和处理新连接，指定 NioServerSocketChannel（5）为信道类型。在此之后，我们设置本地地址是 InetSocketAddress 与所选择的端口（6）如。服务器将绑定到此地址来监听新的连接请求。

第七步是关键：在这里我们使用一个特殊的类，ChannelInitializer 。当一个新的连接被接受，一个新的子 Channel 将被创建， ChannelInitializer 会添加我们EchoServerHandler 的实例到 Channel 的 ChannelPipeline。正如我们如前所述，这个处理器将被通知如果有入站信息。

虽然 NIO 是可扩展性，但它的正确配置是不简单的。特别是多线程，要正确处理也非易事。幸运的是，Netty 的设计封装了大部分复杂性，尤其是通过抽象，例如 EventLoopGroup，SocketChannel 和 ChannelInitializer，其中每一个将在更详细地在第3章中讨论。

在步骤8，我们绑定的服务器，等待绑定完成。 （调用 sync() 的原因是当前线程阻塞）在第9步的应用程序将等待服务器 Channel 关闭（因为我们 在 Channel 的 CloseFuture 上调用 sync()）。现在，我们可以关闭下 EventLoopGroup 并释放所有资源，包括所有创建的线程（10）。

NIO 用于在本实施例，因为它是目前最广泛使用的传输，归功于它的可扩展性和彻底的不同步。但不同的传输的实现是也是可能的。例如，如果本实施例中使用的 OIO 传输，我们将指定 OioServerSocketChannel 和 OioEventLoopGroup。 Netty 的架构，包括更关于传输信息，将包含在第4章。在此期间，让我们回顾下在服务器上执行，我们只研究重要步骤。

服务器的主代码组件是

- EchoServerHandler 实现了的业务逻辑
- 在 main() 方法，引导了服务器

执行后者所需的步骤是：

- 创建 ServerBootstrap 实例来引导服务器并随后绑定
- 创建并分配一个 NioEventLoopGroup 实例来处理事件的处理，如接受新的连接和读/写数据。
- 指定本地 InetSocketAddress 给服务器绑定
- 通过 EchoServerHandler 实例给每一个新的 Channel 初始化
- 最后调用 ServerBootstrap.bind() 绑定服务器

这样服务器初始化完成，可以被使用了。

### 2.4写一个 echo 客户端

客户端要做的是：

- 连接服务器
- 发送信息
- 发送的每个信息，等待和接收从服务器返回的同样的信息
- 关闭连接

#### 2.4.1用 ChannelHandler 实现客户端逻辑

跟写服务器一样，我们提供 ChannelInboundHandler 来处理数据。下面例子，我们用 SimpleChannelInboundHandler 来处理所有的任务，需要覆盖三个方法：

- channelActive() - 服务器的连接被建立后调用
- channelRead0() - 数据后从服务器接收到调用
- exceptionCaught() - 捕获一个异常时调用

Listing 2.4 ChannelHandler for the client

```
@Sharable                                //1
public class EchoClientHandler extends
        SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", //2
        CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
        ByteBuf in) {
        System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8));    //3
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
        Throwable cause) {                    //4
        cause.printStackTrace();
        ctx.close();
    }
}
```

> 1.`@Sharable`标记这个类的实例可以在 channel 里共享
>
> 2.当被通知该 channel 是活动的时候就发送信息
>
> 3.记录接收到的消息
>
> 4.记录日志错误并关闭 channel

建立连接后该 channelActive() 方法被调用一次。逻辑很简单：一旦建立了连接，字节序列被发送到服务器。该消息的内容并不重要;在这里，我们使用了 Netty 编码字符串 “Netty rocks!” 通过覆盖这种方法，我们确保东西被尽快写入到服务器。

接下来，我们覆盖方法 channelRead0()。这种方法会在接收到数据时被调用。注意，由服务器所发送的消息可以以块的形式被接收。即，当服务器发送 5 个字节是不是保证所有的 5 个字节会立刻收到 - 即使是只有 5 个字节，channelRead0() 方法可被调用两次，第一次用一个ByteBuf（Netty的字节容器）装载3个字节和第二次一个 ByteBuf 装载 2 个字节。唯一要保证的是，该字节将按照它们发送的顺序分别被接收。 （注意，这是真实的，只有面向流的协议如TCP）。

第三个方法重写是 exceptionCaught()。正如在 EchoServerHandler （清单2.2），所述的记录 Throwable 并且关闭通道，在这种情况下终止 连接到服务器。

*SimpleChannelInboundHandler vs. ChannelInboundHandler*

*何时用这2个要看具体业务的需要。在客户端，当 channelRead0() 完成，我们已经拿到的入站的信息。当方法返回，SimpleChannelInboundHandler 会小心的释放对 ByteBuf（保存信息） 的引用。而在 EchoServerHandler,我们需要将入站的信息返回给发送者，write() 是异步的在 channelRead()返回时，可能还没有完成。所以，我们使用 ChannelInboundHandlerAdapter,无需释放信息。最后在 channelReadComplete() 我们调用 ctxWriteAndFlush() 来释放信息。详见第5、6章*

#### 2.4.2引导客户端

客户端引导需要 host 、port 两个参数连接服务器。

Listing 2.5 Main class for the client

```
public class EchoClient {

    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();                //1
            b.group(group)                                //2
             .channel(NioSocketChannel.class)             //3
             .remoteAddress(new InetSocketAddress(host, port))    //4
             .handler(new ChannelInitializer<SocketChannel>() {    //5
                 @Override
                 public void initChannel(SocketChannel ch) 
                     throws Exception {
                     ch.pipeline().addLast(
                             new EchoClientHandler());
                 }
             });

            ChannelFuture f = b.connect().sync();        //6

            f.channel().closeFuture().sync();            //7
        } finally {
            group.shutdownGracefully().sync();            //8
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: " + EchoClient.class.getSimpleName() +
                    " <host> <port>");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        new EchoClient(host, port).start();
    }
}
```

> 1.创建 Bootstrap
>
> 2.指定 EventLoopGroup 来处理客户端事件。由于我们使用 NIO 传输，所以用到了 NioEventLoopGroup 的实现
>
> 3.使用的 channel 类型是一个用于 NIO 传输
>
> 4.设置服务器的 InetSocketAddress
>
> 5.当建立一个连接和一个新的通道时，创建添加到 EchoClientHandler 实例 到 channel pipeline
>
> 6.连接到远程;等待连接完成
>
> 7.阻塞直到 Channel 关闭
>
> 8.调用 shutdownGracefully() 来关闭线程池和释放所有资源

与以前一样，在这里使用了 NIO 传输。请注意，您可以在 客户端和服务器 使用不同的传输 ，例如 NIO 在服务器端和 OIO 客户端。在第四章中，我们将研究一些具体的因素和情况，这将导致 您可以选择一种传输，而不是另一种。

让我们回顾一下我们在本节所介绍的要点

- 一个 Bootstrap 被创建来初始化客户端
- 一个 NioEventLoopGroup 实例被分配给处理该事件的处理，这包括创建新的连接和处理入站和出站数据
- 一个 InetSocketAddress 为连接到服务器而创建
- 一个 EchoClientHandler 将被安装在 pipeline 当连接完成时
- 之后 Bootstrap.connect（）被调用连接到远程的 - 本例就是 echo(回声)服务器。

### 2.5编译和运行 Echo 服务器和客户端

```java

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



```

```java
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

```

![image-20210513200827002](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513200827002.png)

![image-20210513200942418](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513200942418.png)

## 3.Netty 快速入门

### 3.1BOOTSTRAP

#### 3.1.1简介

Netty 应用程序通过设置 bootstrap（引导）类的开始，该类提供了一个 用于应用程序网络层配置的容器。Bootstrap类包含两个子类，`Bootstrap`及`ServerBootstrap`，分别对应于客户端应用及服务端应用，他们的区别在于，服务端需要两个Channel，父Channel用于建立连接，子Channel用于管理已经建立的连接。

#### 3.1.2Bootstrap常用API

group()，指定所要使用的EventLoopGroup
channel()，选择所要使用的channel
localAddress()，指定所要绑定的地址
option()，设置ChannelOption
handler()，设置ChannelHandler
remoteAddress()，设置远程地址
connect()，连接到远程服务，并且返回一个ChannelFuture，用于通知结果
bind()，绑定Channel，并且返回一个ChannelFuture，用于通知绑定结果

### 3.2CHANNEL

#### 3.2.1简介

底层网络传输 API 必须提供给应用 I/O操作的接口，如读，写，连接，绑定等等。对于我们来说，这是结构几乎总是会成为一个“socket”。 Netty 中的接口 Channel 定义了与 socket 丰富交互的操作集：bind, close, config, connect, isActive, isOpen, isWritable, read, write 等等。 Netty 提供大量的 Channel 实现来专门使用。这些包括 AbstractChannel，AbstractNioByteChannel，AbstractNioChannel，EmbeddedChannel， LocalServerChannel，NioSocketChannel 等等。

#### 3.2.2Channel的生命周期状态

ChannelUnregistered     Channel 已经被创建，但还未注册到EventLoop
ChannelRegistered     Channel 已经被注册到了EventLoop
ChannelActive      Channel 处于活动状态（已经连接到它的远程节点），可以接收和发送数据了
ChannelInactive     Channel 没有连接到远程节点

#### 3.2.3常用方法

![image-20210513215003346](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513215003346.png)

> 1.eventLoop()  返回分配给Channel 的EventLoop
> 2.pipeline()  返回分配给Channel 的ChannelPipeline
> 3.isActive()  如果Channel 是活动的，则返回true。活动的意义可能依赖于底层的传输。例如，一个4.Socket 传输一旦连接到了远程节点便是活动的，而一个Datagram 传输一旦被打开便是活动的。
> 5.localAddress()  返回本地的SokcetAddress
> 6.remoteAddress()  返回远程的SocketAddress
> 7.write()  将数据写到远程节点。这个数据将被传递给ChannelPipeline，并且排队直到它被冲刷
> 8.flush()  将之前已写的数据冲刷到底层传输，如一个Socket
> 9.writeAndFlush()  一个简便的方法，等同于调用write()并接着调用flush()
>

### 3.3CHANNELHANDLER

#### 3.3.1ChannelHandler的继承关系

![image-20210513220301238](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513220301238.png)

​	ChannelHandler 支持很多协议，并且提供用于数据处理的容器。我们已经知道 ChannelHandler 由特定事件触发。 ChannelHandler 可专用于几乎所有的动作，包括将一个对象转为字节（或相反），执行过程中抛出的异常处理。

​	ChannelHandler 类似于 servlet 的 Filter 过滤器，负责对 IO 事件或者 IO 操作进行拦截和处理，他可以选择性的拦截和处理自己感兴趣的事件，也可以透传和终止事件的传递，基于 ChannelHandler 接口，用户可以方便的进行业务逻辑定制，例如打印日志，统一封装异常信息，性能统计和消息编解码等。

​	常用的一个接口是 ChannelInboundHandler，这个类型接收到入站事件（包括接收到的数据）可以处理应用程序逻辑。当你需要提供响应时，你也可以从 ChannelInboundHandler 冲刷数据。一句话，业务逻辑经常存活于一个或者多个 ChannelInboundHandler。

#### 3.3.2ChannelHandler 支持的注解

**Sharable**：多个 ChannelPipeline 共有同一个 ChannelHandler

**Skip**：被 skip 注解的方法不会被调用，直接被忽略

#### 3.3.3ChannelHandlerAdapter来由

​	对于大多数 ChannelHandler 会选择性的拦截和处理某个或者某些事件，其他事件会忽略，由下一个 Handler 进行拦截和处理，这就导致用户的 ChannelHandler 必须实现所有接口，这样就形成了代码冗余，可维护性差。

​	为了解决这个问题，Netty 提供了 ChannelHandlerAdapter 基类，他的所有接口实现都是事件透传，如果用户 ChannelHandler 关心某个事件，只需要覆盖 ChannelHandlerAdapter 对应的方法即可，这样类的代码就会非常简洁和清晰。

​	![image-20210513220544988](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513220544988.png)

#### 3.3.4**ChannelHandlerContext详解**

![image-20210513220620596](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513220620596.png)

上图handler结构，每个Channel对应一个ChannelPipeline，而Handler仅仅为处理自己的逻辑而生，他是无状态的，一个ChannelPipeline可以有很多个Handler，为了将Handler和Pipeline联系起来，需要一个中间角色，它就是——ChannelHandlerContext。

所以，ChannelPipeline 中维护的，是一个由 ChannelHandlerContext 组成的双向链表。这个链表的头是 HeadContext, 链表的尾是 TailContext。而无状态的Handler，作为Context的成员，关联在ChannelHandlerContext 中。在对应关系上，每个 ChannelHandlerContext 中仅仅关联着一个 ChannelHandler。

### 3.4CHANNELPIPELINE

ChannelPipeline 提供了一个容器给 ChannelHandler 链并提供了一个API 用于管理沿着链入站和出站事件的流动。每个 Channel 都有自己的ChannelPipeline，当 Channel 创建时自动创建的。 ChannelHandler 是如何安装在 ChannelPipeline？ 主要是实现了ChannelHandler 的抽象 ChannelInitializer。ChannelInitializer子类 通过 ServerBootstrap 进行注册。当它的方法 initChannel() 被调用时，这个对象将安装自定义的 ChannelHandler 集到 pipeline。当这个操作完成时，ChannelInitializer 子类则 从 ChannelPipeline 自动删除自身。

### 3.5EVENTLOOP

EventLoop定义了Netty 的核心抽象，用于处理连接的生命周期中所发生的事件。io.netty.util.concurrent 包构建在JDK 的java.util.concurrent 包上。一个EventLoop 将由一个永远都不会改变的Thread 驱动，同时任务（Runnable 或者Callable）可以直接提交给EventLoop 实现，以立即执行或者调度执行。根据配置和可用核心的不同，可能会创建多个EventLoop 实例用以优化资源的使用，并且单个EventLoop 可能会被指派用于服务多个Channel，如下：

![image-20210513215302825](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513215302825.png)一个EventLoopGroup 包含


1.一个或者多个EventLoop
2.一个EventLoop 在它的生命周期内只和一个Thread 绑定
3.所有由EventLoop 处理的I/O 事件都将在它专有的Thread 上被处理
4.一个Channel 在它的生命周期内只注册于一个EventLoop
5.一个EventLoop 可能会被分配给一个或多个Channel

### 3.6CHANNELFUTURE

Netty 所有的 I/O 操作都是异步。因为一个操作可能无法立即返回，我们需要有一种方法在以后确定它的结果。出于这个目的，Netty 提供了接口 ChannelFuture,它的 addListener 方法注册了一个 ChannelFutureListener ，当操作完成时，可以被通知（不管成功与否）。

#### 3.6.1java.util.concurrent.Future

![image-20210513220905515](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513220905515.png)

从这个类的注释中，我们可以了解到：

> Future 类就是代表了异步计算的结果，这个接口的主要方法就是检查计算是否已完成，等待计算，然后返回计算结果。
>
> 当计算完成后，结果只能通过get方法返回；如果有必要会堵塞直到它计算完成。
>
> 可以通过cancel方法取消。增加的方法来判断任务是否正常完成或者被取消。一旦计算已经完成，计算不能被取消。
>
> 如果你想要使用Future 来取消，但是不提供一个可用的结果，你可以声明Futrue 的类型，但会返回null 作为一个基本任务的结果。
>
> FutureTask 类是Futrue类的一个实现类，实现了Runnable接口，可以被Executor 执行。
> 



#### 3.6.2java.util.concurrent.FutureTask

![image-20210513220950443](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513220950443.png)



一个可取消的异步计算，这个类提供了对Future 的基本实现。有对计算的启动和取消方法，查询计算是否已完成，以及返回计算的结果。

计算的结果只有在计算已完成后才能返回，如果计算没有完成，get方法会堵塞。一旦计算已经完成，计算不能被重启或取消。（除非计算是被runAndReset方法调用）

可以看到这个类中有表示计算执行的状态：

```java
    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;
```

在判断当前计算是否已完成等状态时，都是通过这个字段来进行判断。

#### 3.6.3io.netty.util.concurrent.Future

![image-20210513221127864](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513221127864.png)

可以看到这个Future 接口集成自jdk 的Futrue 接口.

#### 3.6.4io.netty.channel.ChannelFuture

![image-20210513221209588](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513221209588.png)

异步IO操作的结果。

Netty 里面的IO操作全部是异步的。这意味着，IO操作会立即返回，但是在调用结束时，无法保证IO操作已完成。取而代之，将会返回给你一个ChannelFuture 实例，提供IO操作的结果信息或状态。

一个ChannelFuture 要么是未完成，要么是已完成。当一个IO操作开始，一个新的future对象被创建。这个新的future 初始化未完成 - 它既不是成功，也不是失败，也不是被取消。因为IO操作还没有完全结束。如果IO操作已经完成，那它要么是成功，要么是失败，要么是被取消，这个future会被标记成已完成并伴随其他信息，比如失败的原因。请注意，即使是失败和被取消已归属于完成状态。

```java
                                      +---------------------------+
                                      | Completed successfully    |
                                      +---------------------------+
                                 +---->      isDone() = true      |
 +--------------------------+    |    |   isSuccess() = true      |
 |        Uncompleted       |    |    +===========================+
 +--------------------------+    |    | Completed with failure    |
 |      isDone() = false    |    |    +---------------------------+
 |   isSuccess() = false    |----+---->   isDone() = true         |
 | isCancelled() = false    |    |    | cause() = non-null     |
 |    cause() = null     |    |    +===========================+
 +--------------------------+    |    | Completed by cancellation |
                                 |    +---------------------------+
                                 +---->      isDone() = true      |
                                      | isCancelled() = true      |
                                      +---------------------------+
```



## 4.netty架构组件

### 4.1Netty的Channel, Event 和 I/O

**Netty**是一个异步事件驱动的NIO框架，Netty的所有IO操作都是异步非阻塞的。Netty 实际上是使用 Threads（多线程）处理 I/O 事件，熟悉多线程编程的读者可能会需要关注同步代码。但是这么做不好，因为同步会影响程序的性能，Netty 的设计保证程序处理事件不会有同步。图 Figure 3.1 展示了，你不需要在 Channel 之间共享 ChannelHandler 实例的原因：

![image-20210513221411433](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513221411433.png)

该图显示，一个 EventLoopGroup 具有一个或多个 EventLoop。想象 EventLoop 作为一个 Thread 给 Channel 执行工作。 （事实上，一个 EventLoop 是势必为它的生命周期一个线程。）

当创建一个 Channel，Netty 通过 一个单独的 EventLoop 实例来注册该 Channel（并同样是一个单独的 Thread）的通道的使用寿命。这就是为什么你的应用程序不需要同步 Netty 的 I/O操作;所有 Channel 的 I/O 始终用相同的线程来执行。

### 4.2Netty中 Bootstrapping 的作用

**Bootstrapping**是什么？它在Netty中有什么作用呢？Bootstrapping（引导） 是出现在Netty 配置程序的过程中，Bootstrapping在给服务器绑定指定窗口或者要连接客户端的时候会使用到。

Bootstrapping 有以下两种类型：



- 一种是用于客户端的Bootstrap
- 一种是用于服务端的ServerBootstrap



不管程序使用哪种协议，创建的是一个客户端还是服务器，“引导”都是必须要使用到的。

*面向连接 vs. 无连接*

*请记住，这个讨论适用于 TCP 协议，它是“面向连接”的。这样协议保证该连接的端点之间的消息的有序输送。无连接协议发送的消息，无法保证顺序和成功性*

两种 Bootstrapping 之间有一些相似之处，也有一些不同。Bootstrap 和 ServerBootstrap 之间的差异如下：

**Table 3.1 Comparison of Bootstrap classes**

| 分类                | Bootstrap            | ServerBootstrap |
| ------------------- | -------------------- | --------------- |
| 网络功能            | 连接到远程主机和端口 | 绑定本地端口    |
| EventLoopGroup 数量 | 1                    | 2               |

Bootstrap用来连接远程主机，有1个EventLoopGroup

ServerBootstrap用来绑定本地端口，有2个EventLoopGroup

事件组(Groups)，传输(transports)和处理程序(handlers)分别在本章后面讲述，我们在这里只讨论两种"引导"的差异(Bootstrap和ServerBootstrap)。第一个差异很明显，“ServerBootstrap”监听在服务器监听一个端口轮询客户端的“Bootstrap”或DatagramChannel是否连接服务器。通常需要调用“Bootstrap”类的connect()方法，但是也可以先调用bind()再调用connect()进行连接，之后使用的Channel包含在bind()返回的ChannelFuture中。

一个 ServerBootstrap 可以认为有2个 Channel 集合，第一个集合包含一个单例 ServerChannel，代表持有一个绑定了本地端口的 socket；第二集合包含所有创建的 Channel，处理服务器所接收到的客户端进来的连接。下图形象的描述了这种情况：

![image-20210513221601175](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513221601175.png)

与 ServerChannel 相关 EventLoopGroup 分配一个 EventLoop 是 负责创建 Channels 用于传入的连接请求。一旦连接接受，第二个EventLoopGroup 分配一个 EventLoop 给它的 Channel。

## 5.Netty核心之Transport（传输）

### 5.1简介

网络应用程序让人与系统之间可以进行通信，当然网络应用程序也可以将大量的数据从一个地方转移到另一个地方。如何做到这一点取决于具体的网络传输，但转移始终是相同的：字节通过线路。传输的概念帮助我们抽象掉的底层数据转移的机制。所有人都需要知道的是，字节在被发送和接收。

当你做过Java中的网络编程的时候，你应该会发现要支持的并发连接会比预期中要多得多，当然这只是在某些时候会出现的情况。如果你再尝试从阻塞切换到非阻塞传输，则可能遇会到的问题，因为 Java 的公开的网络 API 来处理这两种情况有很大的不同。

Netty 在传输层的API是统一的，这使得比你用 JDK 实现更简单。你无需重构整个代码库，然后将时间花到其他更值得去做的事情上

### 5.2使用不同方式实现IO/NIO

#### 5.2.1不用 Netty 实现 I/O 和 NIO

我们将不用 Netty 实现 I/O 和 NIO，而是使用 JDK API 来实现 I/O 和 NIO。下面这个例子，是使用阻塞 IO 实现的例子：

```java
public class PlainOioServer {

    public void serve(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);     //1
        try {
            for (;;) {
                final Socket clientSocket = socket.accept();    //2
                System.out.println("Accepted connection from " + clientSocket);

                new Thread(new Runnable() {                        //3
                    @Override
                    public void run() {
                        OutputStream out;
                        try {
                            out = clientSocket.getOutputStream();
                            out.write("Hi!\r\n".getBytes(Charset.forName("UTF-8")));                            //4
                            out.flush();
                            clientSocket.close();                //5

                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                clientSocket.close();
                            } catch (IOException ex) {
                                // ignore on close
                            }
                        }
                    }
                }).start();                                        //6
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

1.绑定服务器到指定的端口。

2.接受一个连接。

3.创建一个新的线程来处理连接。

4.将消息发送到连接的客户端。

5.一旦消息被写入和刷新时就 关闭连接。

6.启动线程。

上面的方式可以工作正常，但是这种阻塞模式在大连接数的情况就会有很严重的问题，如客户端连接超时，服务器响应严重延迟，性能无法扩展。为了解决这种情况，我们可以使用异步网络处理所有的并发连接，但问题在于 NIO 和 OIO 的 API 是完全不同的，所以一个用 OIO 开发的网络应用程序想要使用 NIO 重构代码几乎是重新开发。

#### 5.2.2使用 NIO 实现的例子

```java
public class PlainNioServer {
    public void serve(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ss.bind(address);                                            //1
        Selector selector = Selector.open();                        //2
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);    //3
        final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
        for (;;) {
            try {
                selector.select();                                    //4
            } catch (IOException ex) {
                ex.printStackTrace();
                // handle exception
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();    //5
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {                //6
                        ServerSocketChannel server =
                                (ServerSocketChannel)key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE |
                                SelectionKey.OP_READ, msg.duplicate());    //7
                        System.out.println(
                                "Accepted connection from " + client);
                    }
                    if (key.isWritable()) {                //8
                        SocketChannel client =
                                (SocketChannel)key.channel();
                        ByteBuffer buffer =
                                (ByteBuffer)key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {        //9
                                break;
                            }
                        }
                        client.close();                    //10
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                        // 在关闭时忽略
                    }
                }
            }
        }
    }
}
```

1.绑定服务器到制定端口

2.打开 selector 处理 channel

3.注册 ServerSocket 到 ServerSocket ，并指定这是专门意接受 连接。

4.等待新的事件来处理。这将阻塞，直到一个事件是传入。

5.从收到的所有事件中 获取 SelectionKey 实例。

6.检查该事件是一个新的连接准备好接受。

7.接受客户端，并用 selector 进行注册。

8.检查 socket 是否准备好写数据。

9.将数据写入到所连接的客户端。如果网络饱和，连接是可写的，那么这个循环将写入数据，直到该缓冲区是空的。

10.关闭连接。

#### 5.2.3采用 Netty 实现阻塞 I/O 

```java
public class NettyOioServer {

    public void server(int port) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
        EventLoopGroup group = new OioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();        //1

            b.group(group)                                    //2
             .channel(OioServerSocketChannel.class)
             .localAddress(new InetSocketAddress(port))
             .childHandler(new ChannelInitializer<SocketChannel>() {//3
                 @Override
                 public void initChannel(SocketChannel ch) 
                     throws Exception {
                     ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {            //4
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) throws Exception {
                             ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);//5
                         }
                     });
                 }
             });
            ChannelFuture f = b.bind().sync();  //6
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();        //7
        }
    }
}
```

1.创建一个 ServerBootstrap

2.使用 NioEventLoopGroup 允许非阻塞模式（NIO）

3.指定 ChannelInitializer 将给每个接受的连接调用

4.添加的 ChannelHandler 拦截事件，并允许他们作出反应

5.写信息到客户端，并添加 ChannelFutureListener 当一旦消息写入就关闭连接

6.绑定服务器来接受连接

7.释放所有资源

#### 5.2.4采用Netty实现非阻塞I/O

```java
public class NettyNioServer {

    public void server(int port) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();    //1
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup())   //2
             .channel(NioServerSocketChannel.class)
             .localAddress(new InetSocketAddress(port))
             .childHandler(new ChannelInitializer<SocketChannel>() {    //3
                 @Override
                 public void initChannel(SocketChannel ch) 
                     throws Exception {
                     ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {    //4
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) throws Exception {
                             ctx.writeAndFlush(buf.duplicate())                //5
                                .addListener(ChannelFutureListener.CLOSE);
                         }
                     });
                 }
             });
            ChannelFuture f = b.bind().sync();                    //6
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();                    //7
        }
    }
}
```

1.创建一个 ServerBootstrap

2.使用 NioEventLoopGroup 允许非阻塞模式（NIO）

3.指定 ChannelInitializer 将给每个接受的连接调用

4.添加的 ChannelInboundHandlerAdapter() 接收事件并进行处理

5.写信息到客户端，并添加 ChannelFutureListener 当一旦消息写入就关闭连接

6.绑定服务器来接受连接

7.释放所有资源

我们之前提到过 Netty 使用的是统一的 API，所以 Netty 中实现的每个传输都是用了同样的 API，你使用什么来实现并不在它的关心范围内。Netty 通过操作接口 Channel 、ChannelPipeline 和 ChannelHandler 来实现。

### 5.3基于Netty传输的API

#### 5.3.1Transport API 的核心是 Channel 接口，用于所有的出站操作，见下图

![image-20210513233334081](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513233334081.png)

如上图所示，每个 Channel 都会分配一个 ChannelPipeline 和ChannelConfig。ChannelConfig 负责设置并存储 Channel 的配置，并允许在运行期间更新它们。传输一般有特定的配置设置，可能实现了 ChannelConfig. 的子类型。

ChannelPipeline 容纳了使用的 ChannelHandler 实例，这些ChannelHandler 将处理通道传递的“入站”和“出站”数据以及事件。ChannelHandler 的实现允许你改变数据状态和传输数据。

现在我们可以使用 ChannelHandler 做下面一些事情：

- 传输数据时，将数据从一种格式转换到另一种格式
- 异常通知
- Channel 变为 active（活动） 或 inactive（非活动） 时获得通知* Channel 被注册或注销时从 EventLoop 中获得通知
- 通知用户特定事件

*Intercepting Filter（拦截过滤器）*

*ChannelPipeline 实现了常用的 Intercepting Filter（拦截过滤器）设计模式。UNIX管道是另一例子：命令链接在一起，一个命令的输出连接到 的下一行中的输入。*

你还可以在运行时根据需要添加 ChannelHandler 实例到ChannelPipeline 或从 ChannelPipeline 中删除，这能帮助我们构建高度灵活的 Netty 程序。例如，你可以支持 [STARTTLS](http://en.wikipedia.org/wiki/STARTTLS) 协议，只需通过加入适当的 ChannelHandler（这里是 SslHandler）到的ChannelPipeline 中，当被请求这个协议时。

此外，访问指定的 ChannelPipeline 和 ChannelConfig，你能在Channel 自身上进行操作。Channel 提供了很多方法，如下列表：

#### 5.3.2Channel main methods

|      方法名称      |                        描述                         |
| :----------------: | :-------------------------------------------------: |
|    eventLoop()     |            返回分配给Channel的EventLoop             |
|     pipeline()     |         返回分配给Channel的ChannelPipeline          |
|     isActive()     |    返回Channel是否激活，已激活说明与远程连接对等    |
|   localAddress()   |            返回已绑定的本地SocketAddress            |
|  remoteAddress()   |            返回已绑定的远程SocketAddress            |
|      write()       | 写数据到远程客户端，数据通过ChannelPipeline传输过去 |
|      flush()       |                   刷新先前的数据                    |
| writeAndFlush(...) |  一个方便的方法用户调用write(...)而后调用y flush()  |

后面会越来越熟悉这些方法，现在只需要记住我们的操作都是在相同的接口上运行，Netty 的高灵活性让你可以以不同的传输实现进行重构。

#### 5.3.3写数据到远程已连接客户端可以调用Channel.write()方法，如下代码：

```java
Channel channel = ...; // 获取channel的引用
ByteBuf buf = Unpooled.copiedBuffer("your data", CharsetUtil.UTF_8);            //1
ChannelFuture cf = channel.writeAndFlush(buf); //2

cf.addListener(new ChannelFutureListener() {    //3
    @Override
    public void operationComplete(ChannelFuture future) {
        if (future.isSuccess()) {                //4
            System.out.println("Write successful");
        } else {
            System.err.println("Write error");    //5
            future.cause().printStackTrace();
        }
    }
});
```

1.创建 ByteBuf 保存写的数据

2.写数据，并刷新

3.添加 ChannelFutureListener 即可写操作完成后收到通知，

4.写操作没有错误完成

5.写操作完成时出现错误

Channel 是线程安全(thread-safe)的，它可以被多个不同的线程安全的操作，在多线程环境下，所有的方法都是安全的。正因为 Channel 是安全的，我们存储对Channel的引用，并在学习的时候使用它写入数据到远程已连接的客户端，使用多线程也是如此。下面的代码是一个简单的多线程例子：

#### 5.3.4Using the channel from many threads

```java
final Channel channel = ...; // 获取channel的引用
final ByteBuf buf = Unpooled.copiedBuffer("your data",
        CharsetUtil.UTF_8).retain();    //1
Runnable writer = new Runnable() {        //2
    @Override
    public void run() {
        channel.writeAndFlush(buf.duplicate());
    }
};
Executor executor = Executors.newCachedThreadPool();//3

//写进一个线程
executor.execute(writer);        //4

//写进另外一个线程
executor.execute(writer);        //5
```

1.创建一个 ByteBuf 保存写的数据

2.创建 Runnable 用于写数据到 channel

3.获取 Executor 的引用使用线程来执行任务

4.手写一个任务，在一个线程中执行

5.手写另一个任务，在另一个线程中执行

### 5.4Netty中包含的 Transport

虽然**Netty**不能支持所有的传输协议，但是Netty自身是携带了一些传输协议的，这些Netty自带的传输协议已经能够满足我们的使用。Netty应用程序的传输协议依赖的是底层协议，接下来我们学习的内容就是Netty中包含的传输协议。

#### 5.4.1Netty中的传输方式有如下几种：

| 方法名称 |             包              |                             描述                             |
| :------: | :-------------------------: | :----------------------------------------------------------: |
|   NIO    | io.netty.channel.socket.nio |  基于java.nio.channels的工具包，使用选择器作为基础的方法。   |
|   OIO    | io.netty.channel.socket.oio |              基于java.net的工具包，使用阻塞流。              |
|  Local   |   io.netty.channel.local    |                  用来在虚拟机之间本地通信。                  |
| Embedded |  io.netty.channel.embedded  | 嵌入传输，它允许在没有真正网络的传输中使用 ChannelHandler，可以非常有用的来测试ChannelHandler的实现。 |

##### NIO-Nonblocking I/O

NIO传输是目前最常用的方式，它通过使用选择器提供了完全异步的方式操作所有的 I/O，NIO 从Java 1.4才被提供。

NIO 中，我们可以注册一个通道或获得某个通道的改变的状态，通道状态有下面几种改变：

- 一个新的 Channel 被接受并已准备好
- Channel 连接完成
- Channel 中有数据并已准备好读取
- Channel 发送数据出去

处理完改变的状态后需重新设置他们的状态，用一个线程来检查是否有已准备好的 Channel，如果有则执行相关事件。在这里可能只同时一个注册的事件而忽略其他的。选择器所支持的操作在 SelectionKey 中定义，具体如下：

Table 4.2 Selection operation bit-set

|  方法名称  |                   描述                   |
| :--------: | :--------------------------------------: |
| OP_ACCEPT  |            有新连接时得到通知            |
| OP_CONNECT |            连接完成后得到通知            |
|  OP_READ   |         准备好读取数据时得到通知         |
|  OP_WRITE  | 写入更多数据到通道时得到通知，大部分时间 |

这是可能的，但有时 socket 缓冲区完全填满了。这通常发生在你写数据的速度太快了超过了远程节点的处理能力。

![image-20210513235728508](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513235728508.png)

1.新信道注册 WITH 选择器

2.选择处理的状态变化的通知

3.以前注册的通道

4.Selector.select（）方法阻塞，直到新的状态变化接收或配置的超时 已过

5.检查是否有状态变化

6.处理所有的状态变化

7.在选择器操作的同一个线程执行其他任务

有一种功能，目前仅适用于 NIO 传输叫什么 “zero-file-copy （零文件拷贝）”，这使您能够快速，高效地通过移动数据到从文件系统传输内容 网络协议栈而无需复制从内核空间到用户空间。这可以使 FT P或 HTTP 协议有很大的不同。

然而，并非所有的操作系统都支持此功能。此外，你不能用它实现数据加密或压缩文件系统 - 仅支持文件的原生内容。另一方面，传送的文件原本已经加密的是完全有效的。

接下来，我们将讨论的是 OIO ，它提供了一个阻塞传输。

##### OIO-Old blocking I/O

Netty 中，该 OIO 传输代表了一种妥协。它通过了 Netty 的通用 API 访问但不是异步，而是构建在 java.net 的阻塞实现上。任何人下面讨论这一点可能会认为，这个协议并没有很大优势。但它确实有它有效的用途。

假设你需要的端口使用该做阻塞调用库（例如 [JDBC](http://www.oracle.com/technetwork/java/javase/jdbc/index.html)）。它可能不适合非阻塞。相反，你可以在短期内使用 OIO 传输，后来移植到纯异步的传输上。让我们看看它是如何工作的。

在 java.net API，你通常有一个线程接受新的连接到达监听在ServerSocket，并创建一个新的线程来处理新的 Socket 。这是必需的，因为在一个特定的 socket的每个 I/O 操作可能会阻塞在任何时间。在一个线程处理多个 socket 易造成阻塞操作，一个　socket　占用了所有的其他人。

鉴于此，你可能想知道　Netty　是如何用相同的　API　来支持　NIO　的异步传输。这里的　Netty　利用了　SO_TIMEOUT　标志，可以设置在一个　Socket。这　timeout　指定最大　毫秒　数量　用于等待　I/O　的操作完成。如果操作在指定的时间内失败，SocketTimeoutException　会被抛出。 Netty中捕获该异常并继续处理循环。在接下来的事件循环运行，它再次尝试。像　Netty　的异步架构来支持　OIO　的话，这其实是唯一的办法。当SocketTimeoutException　抛出时，执行　stack trace。

![image-20210513235754964](https://gitee.com/ljf2402901363/picgo-images/raw/master/typora/image-20210513235754964.png)

1.线程分配给 Socket

2.Socket 连接到远程

3.读操作（可能会阻塞）

4.读完成

5.处理可读的字节

6.执行提交到 socket 的其他任务

7.再次尝试读

##### 同个 JVM 内的本地 Transport 通信

Netty 提供了“本地”传输，为运行在同一个 Java 虚拟机上的服务器和客户之间提供异步通信。此传输支持所有的 Netty 常见的传输实现的 API。

在此传输中，与服务器 Channel 关联的 SocketAddress 不是“绑定”到一个物理网络地址中，而是在服务器是运行时它被存储在注册表中，当 Channel 关闭时它会注销。由于该传输不是“真正的”网络通信，它不能与其他传输实现互操作。因此，客户端是希望连接到使用本地传输的的服务器时，要注意正确的用法。除此限制之外，它的使用是与其他的传输是相同的。

##### 内嵌 Transport

Netty中 还提供了可以嵌入 ChannelHandler 实例到其他的 ChannelHandler 的传输，使用它们就像辅助类，增加了灵活性的方法，使您可以与你的 ChannelHandler 互动。

该嵌入技术通常用于测试 ChannelHandler 的实现，但它也可用于将功能添加到现有的 ChannelHandler 而无需更改代码。嵌入传输的关键是Channel 的实现，称为“EmbeddedChannel”。

### 5.5Netty中Transport 的使用情况

前面说了，并不是所有传输都支持核心协议，这会限制你的选择，具体看下表

| Transport | TCP  | UDP  | SCTP* | UDT  |
| :-------: | :--: | :--: | :---: | :--: |
|    NIO    |  X   |  X   |   X   |  X   |
|    OIO    |  X   |  X   |   X   |  X   |

*指目前仅在 Linux 上的支持。

*在 Linux 上启用 SCTP*

注意 SCTP 需要 kernel 支持，举例 Ubuntu：

```
sudo apt-get install libsctp1
```

Fedora 使用 yum:

```
sudo yum install kernel-modules-extra.x86_64 lksctp-tools.x86_64
```

虽然只有 [SCTP](http://www.ietf.org/rfc/rfc2960.txt) 具有这些特殊的要求，对应的特定的传输也有推荐的配置。想想也是，一个服务器平台可能会需要支持较高的数量的并发连接比单个客户端的话。

下面是你可能遇到的用例:

- OIO-在低连接数、需要低延迟时、阻塞时使用
- NIO-在高连接数时使用
- Local-在同一个JVM内通信时使用
- Embedded-测试ChannelHandler时使用