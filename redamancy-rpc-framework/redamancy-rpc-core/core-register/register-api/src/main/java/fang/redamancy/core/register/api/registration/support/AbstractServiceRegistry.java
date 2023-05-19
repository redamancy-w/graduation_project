package fang.redamancy.core.register.api.registration.support;

import cn.hutool.core.collection.ConcurrentHashSet;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.register.api.registration.Register;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;


/**
 * @Author redamancy
 * @Date 2023/2/1 21:38
 * @Version 1.0
 */
@Slf4j
public abstract class AbstractServiceRegistry implements Register {

    protected final Set<RpcConfig> registered = new ConcurrentHashSet<>();

    private RpcConfig registryRpcConfig;

    public AbstractServiceRegistry(RpcConfig rpcConfig) {
        if (rpcConfig == null) {
            log.error("创建注册中心客户端失败:url == null");
            throw new RuntimeException("创建注册中心客户端失败，请查看日志");
        }
        this.registryRpcConfig = rpcConfig;
    }

    @Override
    public RpcConfig getUrl() {
        return null;
    }

    @Override
    public void register(RpcConfig rpcConfig) {
        addUrl(rpcConfig);
    }

    private void checkUrl(RpcConfig rpcConfig) {
        if (rpcConfig == null) {
            throw new IllegalArgumentException("register url == null");
        }
    }

    private void addUrl(RpcConfig rpcConfig) {
        checkUrl(rpcConfig);
        log.info("Register" + rpcConfig);
        registered.add(rpcConfig);
    }

    private void deleteUrl(RpcConfig rpcConfig) {
        checkUrl(rpcConfig);
        log.info("Unregister" + rpcConfig);
        registered.remove(rpcConfig);
    }

    @Override
    public void unregister(RpcConfig rpcConfig) {
        deleteUrl(rpcConfig);
    }


}
