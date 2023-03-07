package fang.redamancy.core.provide;

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
    void addService(URL url, Class<?> interfaceClazz);

    /**
     * 向注册中心查询服务
     *
     * @param url rpc service related attributes
     * @return service object
     */
    Object getService(URL url);

    /**
     * 同addService
     *
     * @param url rpc service related attributes
     */
    void publishService(URL url, Class<?> interfaceClazz);

}
