package fang.redamancy.core.register.api.registration.failback;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fang.redamancy.core.common.constant.nacosattribute.NacosSupport;
import fang.redamancy.core.common.net.support.URL;
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
    private final Set<URL> failedRegistered = new ConcurrentHashSet<URL>();

    public FailbackRegister(URL url) {
        super(url);

        /**
         * 重试的启动时间
         */
        int retryPeriod = url.getParameter(NacosSupport.REGISTRY_RETRY_PERIOD_KEY,
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
    public void register(URL url) {
        super.register(url);
        failedRegistered.remove(url);
        try {
            doRegister(url);
        } catch (Exception e) {
            failedRegistered.add(url);
            log.error("注册失败url:{}", url);
        }
    }

    protected void retry() {
        if (!failedRegistered.isEmpty()) {
            Set<URL> failed = new HashSet<URL>(failedRegistered);
            if (failed.size() > 0) {
                log.info("Retry register " + failed);
                try {
                    for (URL url : failed) {
                        try {
                            doRegister(url);
                            failedRegistered.remove(url);
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
     * @param url
     */
    protected abstract void doRegister(URL url);

    /**
     * 注销服务
     *
     * @param url
     */
    protected abstract void doUnsubscribe(URL url);

}
