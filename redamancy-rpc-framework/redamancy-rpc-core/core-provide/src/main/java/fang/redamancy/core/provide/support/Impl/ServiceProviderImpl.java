package fang.redamancy.core.provide.support.Impl;

import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.provide.support.AbstractServiceProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author redamancy
 * @Date 2023/3/7 20:29
 * @Version 1.0
 */
@Slf4j
public class ServiceProviderImpl extends AbstractServiceProvider {

    public ServiceProviderImpl(URL url) {
        super(url);
    }

    @Override
    protected void doAddService(URL url) {
        try {
            register.register(url);
        } catch (RuntimeException runtimeException) {
            log.error("注册失败：" + url);
        }
    }
}
