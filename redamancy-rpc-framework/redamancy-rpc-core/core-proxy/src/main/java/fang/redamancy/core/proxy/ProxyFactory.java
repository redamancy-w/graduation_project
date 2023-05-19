package fang.redamancy.core.proxy;

import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.model.RpcConfig;

/**
 * 代理对象创建工厂
 * 创建代理对象
 * 和代理对象调用的invoker
 *
 * @Author redamancy
 * @Date 2023/4/9 14:26
 * @Version 1.0
 */
@SPI("jdk")
public interface ProxyFactory {

    /**
     * 创建代理对象
     *
     * @param invoker
     * @return proxy
     */
    <T> T getProxy(Invoker<T> invoker) throws RpcException;

    <B> Invoker<B> refer(Class<B> type, RpcConfig rpcConfig) throws RpcException;
}
