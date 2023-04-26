package fang.redamancy.core.provide.support.Impl;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.provide.support.AbstractServiceProvider;
import fang.redamancy.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author redamancy
 * @Date 2023/3/7 20:29
 * @Version 1.0
 */
@Slf4j
public class ServiceProviderImpl extends AbstractServiceProvider {

    private LoadBalance loadBalance;

    public ServiceProviderImpl(URL url) {
        super(url);
    }

    public ServiceProviderImpl() {
        super();
    }

    @Override
    protected void doAddService(URL url) {
        try {
            register.register(url);
        } catch (RuntimeException runtimeException) {
            log.error("注册失败：" + url);
        }
    }

    @Override
    public URL getAddress(URL url, RpcRequest request) {

        List<URL> urls = register.discoverRegister(url.getRpcServiceKey(null));
        loadBalance = ExtensionLoader.getExtension(LoadBalance.class, url.getParameter(Constants.LOAD_BALANCE, Constants.LOAD_BALANCE_DEFAULT));
        return loadBalance.selectServiceAddress(urls, request);

    }
}
