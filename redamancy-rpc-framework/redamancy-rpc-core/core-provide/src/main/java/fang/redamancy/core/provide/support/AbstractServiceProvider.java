package fang.redamancy.core.provide.support;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.provide.ServiceProvider;
import fang.redamancy.core.register.api.factory.RegisterFactory;
import fang.redamancy.core.register.api.registration.Register;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author redamancy
 * @Date 2023/3/7 20:15
 * @Version 1.0
 */
public abstract class AbstractServiceProvider implements ServiceProvider {

    /**
     * 注册中心工厂
     */
    protected RegisterFactory registerFactory;
    /**
     * 注册中心客户端
     */
    protected Register        register;

    private Map<String, URL> interfaceToUrl = new ConcurrentHashMap<>();

    public AbstractServiceProvider(URL url) {
        String protocol = getProtocol(url);
        registerFactory = ExtensionLoader.getExtension(RegisterFactory.class, protocol);
        register = registerFactory.getRegistryClient(url);
    }

    protected String getProtocol(URL url) {
        return StringUtils.hasText(url.getProtocol()) ? Constants.DEFAULT_PROTOCOL : url.getProtocol();
    }


    @Override
    public void addService(URL url, Class<?> interfaceClazz) {
        if (Objects.isNull(url)) {
            throw new IllegalArgumentException("url 为空");
        }

        String interfaceName =
                StringUtils.hasText(url.getInterfaceName()) ? url.getInterfaceName() : interfaceClazz.getSimpleName();

        interfaceToUrl.put(interfaceName, url);
        doAddService(url);
    }

    protected abstract void doAddService(URL url);

    @Override
    public Object getService(URL url) {
        return null;

    }

    @Override
    public void publishService(URL url, Class<?> interfaceClazz) {
        addService(url, interfaceClazz);
    }

}
