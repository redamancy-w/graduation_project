package fang.redamancy.core.provide.support;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.enums.RpcErrorMessageEnum;
import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.model.RpcConfig;
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

    private static final Map<String, RpcConfig> interfaceToUrl = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> interfaceMap = new ConcurrentHashMap<>();

    /**
     * 储存服务对象
     */
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public AbstractServiceProvider() {
    }

    public AbstractServiceProvider(RpcConfig rpcConfig) {
        String protocol = getProtocol(rpcConfig);
        registerFactory = ExtensionLoader.getExtension(RegisterFactory.class, protocol);
        register = registerFactory.getRegistryClient(rpcConfig);
    }

    protected String getProtocol(RpcConfig rpcConfig) {
        return StringUtils.hasText(rpcConfig.getProtocol()) ? Constants.DEFAULT_PROTOCOL : rpcConfig.getProtocol();
    }


    @Override
    public void addService(RpcConfig rpcConfig, Class<?> interfaceClazz, Object server) {
        if (Objects.isNull(rpcConfig)) {
            throw new IllegalArgumentException("url 为空");
        }

        String interfaceKey = rpcConfig.getRpcServiceKey(interfaceClazz.getName());

        interfaceToUrl.put(interfaceKey, rpcConfig);

        interfaceMap.put(interfaceKey, interfaceClazz);

        serviceMap.put(interfaceKey, server);

        doAddService(rpcConfig);

    }


    protected abstract void doAddService(RpcConfig rpcConfig);

    @Override
    public Object getService(String serverName) {
        Object service = serviceMap.get(serverName);

        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }

        return service;

    }

    @Override
    public void publishService(RpcConfig rpcConfig, Class<?> interfaceClazz, Object server) {
        addService(rpcConfig, interfaceClazz, server);
    }

}
