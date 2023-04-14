package fang.redamancy.core.proxy.support;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.proxy.Invoker;
import fang.redamancy.core.proxy.ProxyFactory;

/**
 * @Author redamancy
 * @Date 2023/4/9 14:46
 * @Version 1.0
 */
public abstract class AbstractProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        Class<?> interfaces = null;
        String config = invoker.getUrl().getParameter("interfaces");
        if (config != null && config.length() > 0) {
            String[] types = Constants.COMMA_SPLIT_PATTERN.split(config);
            if (types != null && types.length > 0) {
                interfaces = invoker.getInterface();
            }
        }
        return getProxy(invoker, interfaces);
    }

    public abstract <T> T getProxy(Invoker<T> invoker, Class<?> types);

}
