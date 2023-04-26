package fang.redamancy.loadbalance;

import cn.hutool.core.collection.CollectionUtil;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.net.support.URL;

import java.util.List;

/**
 * @Author redamancy
 * @Date 2023/4/18 17:35
 * @Version 1.0
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public URL selectServiceAddress(List<URL> serviceUrlList, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            return null;
        }
        if (serviceUrlList.size() == 1) {
            return serviceUrlList.get(0);
        }

        return doSelect(serviceUrlList, rpcRequest);
    }

    //查找算法实现
    protected abstract URL doSelect(List<URL> serviceAddresses, RpcRequest rpcRequest);
}
