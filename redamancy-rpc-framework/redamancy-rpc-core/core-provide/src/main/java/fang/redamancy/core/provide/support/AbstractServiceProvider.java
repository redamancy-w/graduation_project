package fang.redamancy.core.provide.support;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.enums.RpcErrorMessageEnum;
import fang.redamancy.core.common.exception.RpcException;
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
    protected Register register;

    private static final Map<String, URL> interfaceToUrl = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> interfaceMap = new ConcurrentHashMap<>();

    /**
     * 储存服务对象
     */
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public AbstractServiceProvider() {
    }

    public AbstractServiceProvider(URL url) {
        String protocol = getProtocol(url);
        registerFactory = ExtensionLoader.getExtension(RegisterFactory.class, protocol);
        register = registerFactory.getRegistryClient(url);
    }

    protected String getProtocol(URL url) {
        return StringUtils.hasText(url.getProtocol()) ? Constants.DEFAULT_PROTOCOL : url.getProtocol();
    }


    @Override
    public void addService(URL url, Class<?> interfaceClazz, Object server) {
        if (Objects.isNull(url)) {
            throw new IllegalArgumentException("url 为空");
        }
        String interfaceKey = url.getRpcServiceKey(interfaceClazz.getName());
        interfaceToUrl.put(interfaceKey, url);
        interfaceMap.put(interfaceKey, interfaceClazz);
        serviceMap.put(interfaceKey, server);

        doAddService(url);
    }


    protected abstract void doAddService(URL url);

    @Override
    public Object getService(String serverName) {
        Object service = serviceMap.get(serverName);

        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }

        return service;

    }

    @Override
    public void publishService(URL url, Class<?> interfaceClazz, Object server) {
        addService(url, interfaceClazz, server);
    }

}
