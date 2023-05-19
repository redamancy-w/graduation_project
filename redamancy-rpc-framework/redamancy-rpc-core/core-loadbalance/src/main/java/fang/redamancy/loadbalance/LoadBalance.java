package fang.redamancy.loadbalance;

import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.model.RpcRequest;

import java.util.List;

/**
 * @Author redamancy
 * @Date 2023/4/18 17:35
 * @Version 1.0
 */
@SPI
public interface LoadBalance {

    RpcConfig selectServiceAddress(List<RpcConfig> serviceRpcConfigList, RpcRequest rpcRequest);
}
