package fang.redamancy.core.proxy.support;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.proxy.Invoker;
import fang.redamancy.core.proxy.ProxyFactory;
import fang.redamancy.core.proxy.support.impl.RpcInvoker;
import fang.redamancy.core.remoting.transport.RpcRequestTransport;
import org.springframework.util.StringUtils;

/**
 * @Author redamancy
 * @Date 2023/4/9 14:46
 * @Version 1.0
 */
public abstract class AbstractProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        Class<?> interfaces = null;
        interfaces = invoker.getInterface();
        return getProxy(invoker, interfaces);
    }


    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return new RpcInvoker<>(type, url, getClients(url));
    }

    private RpcRequestTransport getClients(URL url) {
        ExtensionLoader<RpcRequestTransport> extensionLoader = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class);
        if (StringUtils.hasText(url.getParameter(Constants.TRANSPORT))) {
            return extensionLoader.getExtension(url.getParameter(Constants.TRANSPORT));
        }
        return extensionLoader.getDefaultExtension();
    }

    public abstract <T> T getProxy(Invoker<T> invoker, Class<?> types);

}
