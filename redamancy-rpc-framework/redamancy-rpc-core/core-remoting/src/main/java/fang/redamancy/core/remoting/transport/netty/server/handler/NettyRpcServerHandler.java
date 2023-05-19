package fang.redamancy.core.remoting.transport.netty.server.handler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.constant.RpcConstants;
import fang.redamancy.core.common.model.RpcMessage;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.model.RpcResponse;
import fang.redamancy.core.common.util.RuntimeUtil;
import fang.redamancy.core.common.util.SingletonFactoryUtil;
import fang.redamancy.core.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @Author redamancy
 * @Date 2023/4/17 17:22
 * @Version 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    private final ScheduledExecutorService scheduledExecutorService;


    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactoryUtil.getInstance(RpcRequestHandler.class);
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(RuntimeUtil.cpus(),
                new ThreadFactoryBuilder()
                        .setNameFormat("FailRegister-pool-")
                        .setDaemon(true)
                        .build()
        );
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        scheduledExecutorService.execute(() -> {

            try {
                if (msg instanceof RpcMessage) {
                    log.info("server receive msg: [{}] ", msg);
                    byte messageType = ((RpcMessage) msg).getMessageType();
                    RpcMessage rpcMessage = new RpcMessage();
                    rpcMessage.setCodec(((RpcMessage) msg).getCodec());
                    rpcMessage.setCompress(((RpcMessage) msg).getCompress());
                    if (messageType == Constants.HEARTBEAT_REQUEST_TYPE) {
                        rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                        rpcMessage.setData(RpcConstants.PONG);
                    } else {
                        RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();

                        Object result = rpcRequestHandler.handle(rpcRequest);
                        log.debug(String.format("server get result: %s", result.toString()));

                        rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                        if (ctx.channel().isActive() && ctx.channel().isWritable() && !(result instanceof Exception)) {
                            RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                            rpcMessage.setData(rpcResponse);
                        } else {
                            RpcResponse<Object> rpcResponse = RpcResponse.fail((Exception) result, rpcRequest.getRequestId());
                            rpcMessage.setData(rpcResponse);
                            log.error("invoker exception");
                        }
                    }
                    ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                }
            } finally {
                //Ensure that ByteBuf is released, otherwise there may be memory leaks
                ReferenceCountUtil.release(msg);
            }

        });

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }

}
