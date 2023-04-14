package fang.redamancy.core.proxy;

import fang.redamancy.core.common.net.support.Node;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * @Author redamancy
 * @Date 2023/4/9 14:28
 * @Version 1.0
 */
public interface Invoker<T> extends Node {

    /**
     * get interface
     *
     * @return
     */
    Class<?> getInterface();

    RemoteInvocationResult invoke(RpcInvocation rpcInvocation);
}
