package fang.redamancy.core.remoting.transport.netty.server;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.common.util.RuntimeUtil;
import fang.redamancy.core.common.util.ThreadPollFactoryUtil;
import fang.redamancy.core.protocol.regulation.RpcDecoder;
import fang.redamancy.core.protocol.regulation.RpcEncoder;
import fang.redamancy.core.remoting.transport.netty.server.handler.NettyRpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author redamancy
 * @Date 2022/11/10 16:40
 * @Version 1.0
 */
@Slf4j
public class NettyRpcServer implements RpcServer {


    public static final String SERVICE_HANDLER_GROUP = "service_handler_group";

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    private io.netty.channel.Channel channel;
    ServerBootstrap bootstarp;

    @Override
    @SneakyThrows
    public void start(URL config) {

        Integer port = getPort(config);

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2, ThreadPollFactoryUtil.createThreadFactory(SERVICE_HANDLER_GROUP, false)
        );
        bootstarp = new ServerBootstrap();
        bootstarp.group(bossGroup, workerGroup)

                .channel(NioServerSocketChannel.class)
                // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 是否开启 TCP 底层心跳机制
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                .option(ChannelOption.SO_BACKLOG, 128)
                // 当客户端第一次进行请求的时候才会进行初始化
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 30 秒之内没有收到客户端请求的话就关闭连接
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcEncoder());
                        p.addLast(new RpcDecoder());
                        p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                    }
                });

        log.info("The rpcServer is ready");
        // 绑定端口，同步等待绑定成功
        ChannelFuture channelFuture = bootstarp.bind(port).syncUninterruptibly();
        channel = channelFuture.channel();
    }

    private Integer getPort(URL config) {
        return config.getParameter(Constants.BIND_PORT, Constants.BIND_PORT_DEFAULT);
    }
}
