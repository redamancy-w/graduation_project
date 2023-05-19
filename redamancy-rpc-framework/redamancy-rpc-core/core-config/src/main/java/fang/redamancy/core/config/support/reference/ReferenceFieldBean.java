package fang.redamancy.core.config.support.reference;

import fang.redamancy.core.common.annotation.FangReference;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.config.support.AbstractServiceConfig;
import fang.redamancy.core.proxy.Invoker;
import fang.redamancy.core.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 * 主要功能就是将客户端中标记访问远程服务的field 替换成自己写的代理对象
 *
 * @Author redamancy
 * @Date 2023/3/26 17:34
 * @Version 1.0
 */
@Slf4j
public class ReferenceFieldBean<T> extends AbstractServiceConfig implements FactoryBean {

    private final ClassLoader classLoader;

    private static final ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getDefaultExtension();

    private transient volatile Invoker<?> invoker;

    public ReferenceFieldBean(FangReference reference, Class<?> interfaceClazz, ClassLoader classLoader,
                              ApplicationContext applicationContext) {
        super(reference);
        this.classLoader = classLoader;
        this.applicationContext = applicationContext;
        this.interfaceClass = interfaceClazz;
        this.interfaceName = interfaceClazz.getName();
    }

    public Object get() {

        fullConfig();
        if (Objects.isNull(ref)) {
            initProxyObj();
        }
        return ref;
    }

    private void initProxyObj() {
        if (isInit()) {
            return;
        }
        setInit(true);
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<fang:reference interface=\"\" /> interface not allow null!");
        }

        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        ref = createProxy();
    }

    @SuppressWarnings({"unchecked"})
    private T createProxy() {
        RpcConfig rpcConfig = loadNodes();
        invoker = proxyFactory.refer(interfaceClass, rpcConfig);

        return (T) proxyFactory.getProxy(invoker);
    }

    @Override
    public Object getObject() throws Exception {
        return get();
    }


    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

}
