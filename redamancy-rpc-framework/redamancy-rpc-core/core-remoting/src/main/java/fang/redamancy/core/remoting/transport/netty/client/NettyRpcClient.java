package fang.redamancy.core.remoting.transport.netty.client;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.constant.RpcConstants;
import fang.redamancy.core.common.enums.CompressTypeEnum;
import fang.redamancy.core.common.enums.SerializationTypeEnum;
import fang.redamancy.core.common.model.RpcInvocation;
import fang.redamancy.core.common.model.RpcMessage;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.model.RpcResponse;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.common.util.SingletonFactoryUtil;
import fang.redamancy.core.protocol.regulation.RpcDecoder;
import fang.redamancy.core.protocol.regulation.RpcEncoder;
import fang.redamancy.core.provide.ServiceProvider;
import fang.redamancy.core.provide.support.Impl.ServiceProviderImpl;
import fang.redamancy.core.remoting.transport.RpcRequestTransport;
import fang.redamancy.core.remoting.transport.netty.client.bufferpool.ChannelProvider;
import fang.redamancy.core.remoting.transport.netty.client.bufferpool.PendingRequest;
import fang.redamancy.core.remoting.transport.netty.client.handler.NettyRpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author redamancy
 * @Date 2022/11/7 15:53
 * @Version 1.0
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private final PendingRequest pendingRequest;

    private final ChannelProvider channelProvider;

    private final static Map<String, String> cache = new HashMap<>();

    @Override
    public Object request(RpcInvocation invocation, int timeout, URL url) {

        RpcRequest rpcRequest = RpcRequest.builder().methodName(invocation.getMethodName())
                .parameters(invocation.getArgs())
                .interfaceName(invocation.getMethod().getDeclaringClass().getName())
                .paramTypes(invocation.getMethod().getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(invocation.getParameter(Constants.GROUP_KEY, Constants.GROUP_DEFAULT))
                .version(invocation.getParameter(Constants.VERSION_KEY, Constants.VERSION_DEFAULT))
                .build();

        if (!cache.containsKey(Constants.SERIALIZE) && !cache.containsKey(Constants.COMPRESS)) {
            cache.put(Constants.SERIALIZE, url.getParameter(Constants.SERIALIZE, Constants.SERIALIZE_DEFAULT));
            cache.put(Constants.COMPRESS, url.getParameter(Constants.COMPRESS, Constants.COMPRESS_DEFAULT));
        }

        ServiceProvider provider = new ServiceProviderImpl(url);
        URL addressUrl = provider.getAddress(url, rpcRequest);
        return send(rpcRequest, timeout, addressUrl.getAddress());
    }

    private Object send(RpcRequest request, Integer timeout, InetSocketAddress inetSocketAddress) {

        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();

        Channel channel = getChannel(inetSocketAddress);

        if (channel.isActive()) {
            pendingRequest.put(request.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(request)
                    .codec(SerializationTypeEnum.getCode(cache.get(Constants.SERIALIZE)))
                    .compress(CompressTypeEnum.getCode(cache.get(Constants.COMPRESS)))
                    .messageType(RpcConstants.REQUEST_TYPE).build();

            ChannelFuture future = channel.writeAndFlush(rpcMessage);
            boolean success = false;

            try {
                success = future.await(timeout);
                Throwable cause = future.cause();
                if (cause != null) {
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
                if (!success) {
                    throw new RuntimeException("远程调用超时,调用信息:" + request);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }


    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        this.pendingRequest = SingletonFactoryUtil.getInstance(PendingRequest.class);
        this.channelProvider = SingletonFactoryUtil.getInstance(ChannelProvider.class);

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
                        p.addLast(new NettyRpcClientHandler());
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
