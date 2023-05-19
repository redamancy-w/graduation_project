package fang.redamancy.core.common.net.support;

import fang.redamancy.core.common.model.RpcConfig;

/**
 * 主机节点
 *
 * @Author redamancy
 * @Date 2023/1/17 20:20
 * @Version 1.0
 */
public interface Node {

    /**
     * 查验注册中心客户端是否可用
     *
     * @return 是否
     */
    boolean isActive();

    RpcConfig getUrl();
}
