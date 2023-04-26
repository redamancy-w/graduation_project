package fang.redamancy.core.proxy.support.impl;

import fang.redamancy.core.proxy.Invoker;
import fang.redamancy.core.proxy.support.AbstractProxyFactory;
import fang.redamancy.core.proxy.support.InvokerInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * @Author redamancy
 * @Date 2023/4/9 14:54
 * @Version 1.0
 */
public class JDKProxyFactory extends AbstractProxyFactory {


    @Override
    public <T> T getProxy(Invoker<T> invoker, Class<?> types) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{types}, new InvokerInvocationHandler(invoker));
    }
}
