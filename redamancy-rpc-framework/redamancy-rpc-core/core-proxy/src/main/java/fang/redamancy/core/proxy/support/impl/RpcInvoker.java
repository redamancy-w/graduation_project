package fang.redamancy.core.proxy.support.impl;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.model.RpcInvocation;
import fang.redamancy.core.common.model.RpcResponse;
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


    public RpcInvoker(Class<T> serviceType, RpcConfig rpcConfig, RpcRequestTransport clients, Invoker invoker) {
        super(serviceType, rpcConfig, new String[]{Constants.INTERFACE_KEY, Constants.GROUP_KEY,
                Constants.TOKEN_KEY, Constants.TIMEOUT_KEY,
                Constants.VERSION_KEY, Constants.COMPRESS,
                Constants.SERIALIZE,
        });
        this.client = clients;
        // get version.
        this.invoker = invoker;
    }

    public RpcInvoker(Class<T> serviceType, RpcConfig rpcConfig, RpcRequestTransport clients) {
        this(serviceType, rpcConfig, clients, null);
    }

    @Override
    protected RpcResponse<?> doInvoke(RpcInvocation invocation) {
        final String methodName = invocation.getMethodName();

        int timeout = getRpcConfig().getMethodParameter(methodName, Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
        return client.request(invocation, timeout, getRpcConfig());

    }

}
