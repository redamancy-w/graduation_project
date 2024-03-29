package fang.redamancy.core.remoting.transport.netty.server;

import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.model.RpcConfig;

/**
 * @Author redamancy
 * @Date 2023/4/25 14:36
 * @Version 1.0
 */
@SPI("netty")
public interface RpcServer {

    void start(RpcConfig config);
}
