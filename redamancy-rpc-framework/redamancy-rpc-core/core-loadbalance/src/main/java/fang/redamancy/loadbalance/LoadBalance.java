package fang.redamancy.loadbalance;

import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.net.support.URL;

import java.util.List;

/**
 * @Author redamancy
 * @Date 2023/4/18 17:35
 * @Version 1.0
 */
@SPI
public interface LoadBalance {

    URL selectServiceAddress(List<URL> serviceUrlList, RpcRequest rpcRequest);
}
