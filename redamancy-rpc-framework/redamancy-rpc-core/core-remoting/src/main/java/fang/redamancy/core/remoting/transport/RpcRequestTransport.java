package fang.redamancy.core.remoting.transport;

/**
 * @Author redamancy
 * @Date 2022/11/7 15:49
 * @Version 1.0
 */

import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.model.RpcInvocation;
import fang.redamancy.core.common.model.RpcResponse;

/**
 * rpc 客户端接口
 */
@SPI("netty")
public interface RpcRequestTransport {


    /**
     * 请求
     *
     * @param invocation 调用
     * @param timeout    超时
     * @return {@link Object}
     */

    RpcResponse request(RpcInvocation invocation, int timeout, RpcConfig rpcConfig);
}
