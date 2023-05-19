package fang.redamancy.core.provide;

import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.model.RpcRequest;

/**
 * @Author redamancy
 * @Date 2023/3/7 20:05
 * @Version 1.0
 */
public interface ServiceProvider {

    /**
     * 向注册中心添加服务
     *
     * @param rpcConfig rpc service related attributes
     */
    void addService(RpcConfig rpcConfig, Class<?> interfaceClazz, Object server);

    /**
     * 得到服务
     *
     * @param servName 服务名字
     * @return service object
     */
    Object getService(String servName);

    /**
     * 同addService
     *
     * @param rpcConfig rpc service related attributes
     */
    void publishService(RpcConfig rpcConfig, Class<?> interfaceClazz, Object server);

    RpcConfig getAddress(RpcConfig rpcConfig, RpcRequest request);
}
