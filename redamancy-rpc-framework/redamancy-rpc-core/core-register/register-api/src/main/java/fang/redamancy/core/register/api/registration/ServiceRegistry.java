package fang.redamancy.core.register.api.registration;

import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.model.RpcConfig;

import java.util.List;


/**
 * @Author redamancy
 * @Date 2023/1/17 19:50
 * @Version 1.0
 */
@SPI("nacos")
public interface ServiceRegistry {

    void register(RpcConfig rpcConfig);

    void unregister(RpcConfig rpcConfig);

    List<RpcConfig> discoverRegister(String serviceKey);
}
