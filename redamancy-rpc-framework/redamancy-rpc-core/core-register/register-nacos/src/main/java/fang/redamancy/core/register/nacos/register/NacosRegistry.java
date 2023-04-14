package fang.redamancy.core.register.nacos.register;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fang.redamancy.core.common.constant.nacosattribute.NacosSupport;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.common.util.NetUtil;
import fang.redamancy.core.common.util.RuntimeUtil;
import fang.redamancy.core.register.api.registration.failback.FailbackRegister;
import lombok.extern.slf4j.Slf4j;

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
        return url.getInterfaceName();
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
        instance.setPort(NetUtil.getAvailablePort());
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

    interface NamingServiceCallback {
        void callback(NamingService namingService) throws NacosException;
    }
}
