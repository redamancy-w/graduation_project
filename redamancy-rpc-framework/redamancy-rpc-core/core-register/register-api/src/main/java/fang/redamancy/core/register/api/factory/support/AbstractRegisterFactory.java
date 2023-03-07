package fang.redamancy.core.register.api.factory.support;

import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.register.api.factory.RegisterFactory;
import fang.redamancy.core.register.api.registration.Register;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

/**
 * @Author redamancy
 * @Date 2023/2/4 17:58
 * @Version 1.0
 */
@Slf4j
public abstract class AbstractRegisterFactory implements RegisterFactory {

    /**
     * 锁
     */
    private static final StampedLock           LOCK       = new StampedLock();
    /**
     * 缓存的注册中心
     */
    private static final Map<String, Register> REGISTRIES = new ConcurrentHashMap<String, Register>();

    @Override
    public Register getRegistryClient(URL url) {
        String id    = url.getRpcServiceId();
        long   stamp = LOCK.readLock();

        try {
            Register register = REGISTRIES.get(id);
            if (!Objects.isNull(register)) {
                return register;
            }
            register = buildRegisterClient(url);
            if (Objects.isNull(register)) {
                log.error("注册客户端创建失败,注册信息为:{}", url);
                throw new RuntimeException("客户端创建失败,信息查看日志");
            }
            REGISTRIES.put(id, register);
            return register;
        } finally {
            LOCK.unlockRead(stamp);
        }
    }

    /**
     * 构建服务客户端
     *
     * @param url 客户端url
     * @return 服务客户端
     */
    protected abstract Register buildRegisterClient(URL url);

}
