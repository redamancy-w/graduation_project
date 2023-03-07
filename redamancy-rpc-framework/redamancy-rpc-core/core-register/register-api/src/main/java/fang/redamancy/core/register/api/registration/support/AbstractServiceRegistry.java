package fang.redamancy.core.register.api.registration.support;

import cn.hutool.core.collection.ConcurrentHashSet;
import fang.redamancy.core.common.net.support.URL;
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

    private final Set<URL> registered = new ConcurrentHashSet<>();

    private URL registryUrl;

    public AbstractServiceRegistry(URL url) {
        if (url == null) {
            log.error("创建注册中心客户端失败:url == null");
            throw new RuntimeException("创建注册中心客户端失败，请查看日志");
        }
        this.registryUrl = url;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public void register(URL url) {
        addUrl(url);
    }

    private void checkUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null");
        }
    }

    private void addUrl(URL url) {
        checkUrl(url);
        log.info("Register" + url);
        registered.add(url);
    }

    private void deleteUrl(URL url) {
        checkUrl(url);
        log.info("Unregister" + url);
        registered.remove(url);
    }

    @Override
    public void unregister(URL url) {
        deleteUrl(url);
    }


}
