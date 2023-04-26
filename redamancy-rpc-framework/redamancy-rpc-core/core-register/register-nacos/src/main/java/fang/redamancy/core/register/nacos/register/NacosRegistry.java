package fang.redamancy.core.register.nacos.register;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.constant.nacosattribute.NacosSupport;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.common.util.NetUtil;
import fang.redamancy.core.common.util.RuntimeUtil;
import fang.redamancy.core.register.api.registration.failback.FailbackRegister;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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

    /**
     *
     */
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(RuntimeUtil.cpus(),
            new ThreadFactoryBuilder()
                    .setNameFormat("FailRegister-pool-")
                    .setDaemon(true)
                    .build()
    );

    public NacosRegistry(NamingService namingService, URL url) {
        super(url);
        this.namingService = namingService;
    }

    private String getServiceName(URL url) {
        return url.getRpcServiceKey(null);
    }

    protected void doRegister(URL url) {

        final String serviceName = getServiceName(url);
        final Instance instance = createInstance(url);

        execute(new NamingServiceCallback() {
            @Override
            public void callback(NamingService namingService) throws NacosException {
                namingService.registerInstance(serviceName, instance);
            }
        });
    }

    protected void doUnsubscribe(URL url) {
        final String serviceName = getServiceName(url);
        final Instance instance = createInstance(url);
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

    private Instance createInstance(URL url) {
        String ip = NetUtil.getLocalhost();
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(url.getParameter(Constants.BIND_PORT, Constants.BIND_PORT_DEFAULT));

        instance.setWeight(url.getParameter(NacosSupport.WEIGHT_KEY, NacosSupport.DEFAULT_WEIGHT));
        instance.setMetadata(url.getParameters());

        return instance;
    }

    private void execute(NamingServiceCallback callback) {
        scheduledExecutorService.execute(() -> {
            try {
                callback.callback(namingService);
            } catch (NacosException e) {
                log.error(e.getErrMsg(), e);
            }
        });
    }

    private void executeSyn(NamingServiceCallback callback) {
        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<URL> discoverRegister(String serviceKey) {

        final List<URL> urls = new LinkedList<URL>();
        executeSyn(new NamingServiceCallback() {
            @Override
            public void callback(NamingService namingService) throws NacosException {
                List<Instance> instances = namingService.getAllInstances(serviceKey);
                urls.addAll(buildAddress(instances));
            }
        });
        return urls;
    }

    private Collection<URL> buildAddress(List<Instance> instances) {
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }
        List<URL> urls = new LinkedList<URL>();
        for (Instance instance : instances) {
            URL url = buildURL(instance);
            urls.add(url);
        }
        return urls;
    }

    private URL buildURL(Instance instance) {

        URL url = new URL(instance.getMetadata().get(Constants.PROTOCOL_KEY),
                instance.getIp(),
                instance.getPort(),
                instance.getMetadata());
        return url;

    }


    interface NamingServiceCallback {
        void callback(NamingService namingService) throws NacosException;
    }
}
