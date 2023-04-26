package fang.redamancy.core.remoting.transport;

/**
 * @Author redamancy
 * @Date 2022/11/7 15:49
 * @Version 1.0
 */

import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.model.RpcInvocation;
import fang.redamancy.core.common.net.support.URL;

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

    Object request(RpcInvocation invocation, int timeout, URL url);
}
