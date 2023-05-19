package fang.redamancy.core.register.api.registration.failback;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fang.redamancy.core.common.constant.nacosattribute.NacosSupport;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.util.RuntimeUtil;
import fang.redamancy.core.register.api.registration.support.AbstractServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author redamancy
 * @Date 2023/2/6 21:08
 * @Version 1.0
 */
@Slf4j
public abstract class FailbackRegister extends AbstractServiceRegistry {
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(RuntimeUtil.cpus(),
            new ThreadFactoryBuilder()
                    .setNameFormat("FailRegister-pool-")
                    .setDaemon(true)
                    .build()
    );

    /**
     * 注册失败的容器
     */
    private final Set<RpcConfig> failedRegistered = new ConcurrentHashSet<RpcConfig>();

    public FailbackRegister(RpcConfig rpcConfig) {
        super(rpcConfig);

        /**
         * 重试的启动时间
         */
        int retryPeriod = rpcConfig.getParameter(NacosSupport.REGISTRY_RETRY_PERIOD_KEY,
                NacosSupport.DEFAULT_REGISTRY_RETRY_PERIOD);
        this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                try {
                    retry();
                } catch (Throwable t) { // Defensive fault tolerance
                    log.error("重试失败时发生意外错误, cause: " + t.getMessage(), t);
                }
            }
        }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
    }


    @Override
    public void register(RpcConfig rpcConfig) {
        super.register(rpcConfig);
        failedRegistered.remove(rpcConfig);
        try {
            doRegister(rpcConfig);
        } catch (Exception e) {
            failedRegistered.add(rpcConfig);
            log.error("注册失败url:{}", rpcConfig);
        }
    }

    protected void retry() {
        if (!failedRegistered.isEmpty()) {
            Set<RpcConfig> failed = new HashSet<RpcConfig>(failedRegistered);
            if (failed.size() > 0) {
                log.info("Retry register " + failed);
                try {
                    for (RpcConfig rpcConfig : failed) {
                        try {
                            doRegister(rpcConfig);
                            failedRegistered.remove(rpcConfig);
                        } catch (Throwable t) { // Ignore all the exceptions and wait for the next retry
                            log.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { // Ignore all the exceptions and wait for the next retry
                    log.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
    }

    /**
     * 注册服务 *
     *
     * @param rpcConfig
     */
    protected abstract void doRegister(RpcConfig rpcConfig);

    /**
     * 注销服务
     *
     * @param rpcConfig
     */
    protected abstract void doUnsubscribe(RpcConfig rpcConfig);

}
