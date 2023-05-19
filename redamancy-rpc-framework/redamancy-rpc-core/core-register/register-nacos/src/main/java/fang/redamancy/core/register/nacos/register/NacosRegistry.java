package fang.redamancy.core.register.nacos.register;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.constant.nacosattribute.NacosSupport;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.util.NetUtil;
import fang.redamancy.core.register.api.registration.failback.FailbackRegister;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author redamancy
 * @Date 2023/2/1 22:59
 * @Version 1.0
 */
@Slf4j
public class NacosRegistry extends FailbackRegister {

    /**
     * nacos注册中心客户端活跃的状态码
     */
    private static final String NACOS_IS_ACTIVE_STATE = "UP";

    /**
     * nacos客户端，Naming
     */
    private final NamingService namingService;


    public NacosRegistry(NamingService namingService, RpcConfig rpcConfig) {
        super(rpcConfig);
        this.namingService = namingService;
    }

    private String getServiceName(RpcConfig rpcConfig) {
        return rpcConfig.getRpcServiceKey(null);
    }

    protected void doRegister(RpcConfig rpcConfig) {

        final String serviceName = getServiceName(rpcConfig);
        final Instance instance = createInstance(rpcConfig);

        execute(new NamingServiceCallback() {
            @Override
            public void callback(NamingService namingService) throws NacosException {
                namingService.registerInstance(serviceName, instance);
            }
        });
    }

    protected void doUnsubscribe(RpcConfig rpcConfig) {
        final String serviceName = getServiceName(rpcConfig);
        final Instance instance = createInstance(rpcConfig);
        execute(new NamingServiceCallback() {
            @Override
            public void callback(NamingService namingService) throws NacosException {
                namingService.deregisterInstance(serviceName, instance.getIp(), instance.getPort());
            }
        });
    }

    @Override
    public boolean isActive() {
        return NACOS_IS_ACTIVE_STATE.equals(namingService.getServerStatus());
    }

    private Instance createInstance(RpcConfig rpcConfig) {
        String ip = NetUtil.getLocalhost();
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(rpcConfig.getParameter(Constants.BIND_PORT, Constants.BIND_PORT_DEFAULT));
        instance.setWeight(rpcConfig.getParameter(NacosSupport.WEIGHT_KEY, NacosSupport.DEFAULT_WEIGHT));
        instance.setMetadata(rpcConfig.getParameters());

        return instance;
    }

    private void execute(NamingServiceCallback callback) {

        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            log.error(e.getErrMsg(), e);
            throw new RuntimeException();
        }
    }

    private void executeSyn(NamingServiceCallback callback) {
        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<RpcConfig> discoverRegister(String serviceKey) {

        final List<RpcConfig> rpcConfigs = new LinkedList<RpcConfig>();
        executeSyn(new NamingServiceCallback() {
            @Override
            public void callback(NamingService namingService) throws NacosException {
                List<Instance> instances = namingService.getAllInstances(serviceKey);
                rpcConfigs.addAll(buildAddress(instances));
            }
        });
        return rpcConfigs;
    }


    private Collection<RpcConfig> buildAddress(List<Instance> instances) {
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }
        List<RpcConfig> rpcConfigs = new LinkedList<RpcConfig>();
        for (Instance instance : instances) {
            RpcConfig rpcConfig = buildURL(instance);
            rpcConfigs.add(rpcConfig);
        }
        return rpcConfigs;
    }

    private RpcConfig buildURL(Instance instance) {

        RpcConfig rpcConfig = new RpcConfig(instance.getMetadata().get(Constants.PROTOCOL_KEY),
                instance.getIp(),
                instance.getPort(),
                instance.getMetadata());
        return rpcConfig;

    }


    interface NamingServiceCallback {
        void callback(NamingService namingService) throws NacosException;
    }
}
