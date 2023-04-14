package fang.redamancy.core.proxy;

import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.common.net.support.URL;

/**
 * 代理对象创建工厂
 * 创建代理对象
 * 和代理对象调用的invoker
 *
 * @Author redamancy
 * @Date 2023/4/9 14:26
 * @Version 1.0
 */
public interface ProxyFactory {

    /**
     * 创建代理对象
     *
     * @param invoker
     * @return proxy
     */
    <T> T getProxy(Invoker<T> invoker) throws RpcException;

    /**
     * 创建 invoker.
     *
     * @param <T>
     * @param proxy
     * @param type
     * @param url
     * @return invoker
     */
    <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException;

}
