package fang.redamancy.core.proxy;

import fang.redamancy.core.common.model.RpcInvocation;
import fang.redamancy.core.common.model.RpcResponse;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * 调用执行
 *
 * @Author redamancy
 * @Date 2023/4/9 14:28
 * @Version 1.0
 */
public interface Invoker<T> {


    /**
     * 调用
     *
     * @param rpcInvocation rpc调用
     * @return {@link RemoteInvocationResult}
     */
    RpcResponse invoke(RpcInvocation rpcInvocation);


    /**
     * 获得接口名
     *
     * @return {@link Class}<{@link ?}>
     */
    Class<?> getInterface();
}
