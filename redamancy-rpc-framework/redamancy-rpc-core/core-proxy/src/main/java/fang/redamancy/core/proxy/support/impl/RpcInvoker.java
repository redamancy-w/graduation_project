package fang.redamancy.core.proxy.support.impl;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.model.RpcInvocation;
import fang.redamancy.core.common.model.RpcResponse;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.proxy.Invoker;
import fang.redamancy.core.proxy.support.AbstractInvoker;
import fang.redamancy.core.remoting.transport.RpcRequestTransport;


/**
 * @Author redamancy
 * @Date 2023/4/16 13:00
 * @Version 1.0
 */
public class RpcInvoker<T> extends AbstractInvoker<T> {

    private final RpcRequestTransport client;

    private final Invoker invoker;


    public RpcInvoker(Class<T> serviceType, URL url, RpcRequestTransport clients, Invoker invoker) {
        super(serviceType, url, new String[]{Constants.INTERFACE_KEY, Constants.GROUP_KEY,
                Constants.TOKEN_KEY, Constants.TIMEOUT_KEY,
                Constants.VERSION_KEY, Constants.COMPRESS,
                Constants.SERIALIZE,
        });
        this.client = clients;
        // get version.
        this.invoker = invoker;
    }

    public RpcInvoker(Class<T> serviceType, URL url, RpcRequestTransport clients) {
        this(serviceType, url, clients, null);
    }

    @Override
    protected RpcResponse<?> doInvoke(RpcInvocation invocation) {
        final String methodName = invocation.getMethodName();

        try {

            int timeout = getUrl().getMethodParameter(methodName, Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);


            return (RpcResponse<?>) client.request(invocation, timeout, getUrl());

        } catch (RuntimeException e) {
            throw new RuntimeException("远程调用失败,调用信息" + invocation);
        }
    }
}
