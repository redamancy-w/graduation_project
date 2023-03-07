package fang.redamancy.core.remoting.transport.netty.client;

import fang.redamancy.core.common.constant.RpcConstants;
import fang.redamancy.core.common.enums.CompressTypeEnum;
import fang.redamancy.core.common.enums.SerializationTypeEnum;
import fang.redamancy.core.common.model.RpcMessage;
import fang.redamancy.core.protocol.regulation.RpcDecoder;
import fang.redamancy.core.protocol.regulation.RpcEncoder;
import fang.redamancy.core.remoting.transport.RpcRequestTransport;
import fang.redamancy.core.remoting.transport.netty.client.bufferpool.ChannelProvider;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author redamancy
 * @Date 2022/11/7 15:53
 * @Version 1.0
 */
@Service("nettyRpcClient")
@Slf4j
@Component
public class NettyRpcClient implements RpcRequestTransport {

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    @Resource
    private ChannelProvider channelProvider;


    @Override
    public Object sendRpcRequest(Object request) {
        Channel channel = getChannel(new InetSocketAddress("localhost", 9998));

        if (channel.isActive()) {
            RpcMessage rpcMessage = RpcMessage.builder().data("test")
                    .codec(SerializationTypeEnum.KYRO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage);
        }

        return null;
    }

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // 连接时间设置，如果超过这个时间则连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        //设定IdleStateHandler心跳检测每四秒进行一次写检测，如果四秒内write()方法未被调用则触发一次userEventTrigger()方法
                        //具体方法在NettyClientHandler中的userEventTriggered
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcEncoder());
                        p.addLast(new RpcDecoder());
                    }
                });
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {

        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }


    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);

        if (channel == null || !channel.isActive()) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }

        return channel;
    }

}
