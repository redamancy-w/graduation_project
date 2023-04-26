package fang.redamancy.core.provide;

import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.net.support.URL;

/**
 * @Author redamancy
 * @Date 2023/3/7 20:05
 * @Version 1.0
 */
public interface ServiceProvider {

    /**
     * 向注册中心添加服务
     *
     * @param url rpc service related attributes
     */
    void addService(URL url, Class<?> interfaceClazz, Object server);

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
     * @param url rpc service related attributes
     */
    void publishService(URL url, Class<?> interfaceClazz, Object server);

    URL getAddress(URL url, RpcRequest request);
}
