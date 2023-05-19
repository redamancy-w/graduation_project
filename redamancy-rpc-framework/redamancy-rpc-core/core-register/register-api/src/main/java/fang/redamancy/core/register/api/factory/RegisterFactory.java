package fang.redamancy.core.register.api.factory;

import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.register.api.registration.Register;

/**
 * @Author redamancy
 * @Date 2023/2/4 17:56
 * @Version 1.0
 */

@SPI("nacos")
public interface RegisterFactory {

    /**
     * 创建注册中心客户端
     */
    Register getRegistryClient(RpcConfig rpcConfig);

}
