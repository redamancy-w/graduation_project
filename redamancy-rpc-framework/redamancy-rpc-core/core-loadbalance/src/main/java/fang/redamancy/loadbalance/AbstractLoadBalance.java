package fang.redamancy.loadbalance;

import cn.hutool.core.collection.CollectionUtil;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.model.RpcRequest;

import java.util.List;

/**
 * @Author redamancy
 * @Date 2023/4/18 17:35
 * @Version 1.0
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public RpcConfig selectServiceAddress(List<RpcConfig> serviceRpcConfigList, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceRpcConfigList)) {
            return null;
        }
        if (serviceRpcConfigList.size() == 1) {
            return serviceRpcConfigList.get(0);
        }

        return doSelect(serviceRpcConfigList, rpcRequest);
    }

    //查找算法实现
    protected abstract RpcConfig doSelect(List<RpcConfig> serviceAddresses, RpcRequest rpcRequest);
}
