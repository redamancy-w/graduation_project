package fang.redamancy.core.config.support.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fang.redamancy.core.common.annotation.FangService;
import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.common.util.RuntimeUtil;
import fang.redamancy.core.config.support.AbstractServiceConfig;
import fang.redamancy.core.provide.ServiceProvider;
import fang.redamancy.core.provide.support.Impl.ServiceProviderImpl;
import fang.redamancy.core.remoting.transport.netty.server.RpcServer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 主要功能：
 * 继承ApplicationListener 在spring加载上下文的时候，暴露服务
 * 从ApplicationContext中获取配置好的配置信息
 *
 * @Author redamancy
 * @Date 2023/3/3 13:35
 * @Version 1.0
 */
@Setter
@Getter
@Slf4j
public class ApplicationListenerRegistrar<T> extends AbstractServiceConfig implements InitializingBean, ApplicationListener<ContextRefreshedEvent>, BeanNameAware {

    private Boolean isOpen = false;

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(RuntimeUtil.cpus(),
            new ThreadFactoryBuilder()
                    .setNameFormat("Exposed-Service-pool-")
                    .setDaemon(true)
                    .build()
    );

    public ApplicationListenerRegistrar(FangService service) {
        super(service);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (isDelay() && !isExposed()) {
            log.info("The service ready on spring started. service: " + getInterface());
            export();
        }
    }


    private synchronized void export() {

        if (isExposed()) {
            return;
        }

        if (delay != null && delay > 0) {
            scheduledExecutorService.schedule(new Runnable() {
                public void run() {
                    doExport();
                }
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            doExport();
        }

    }


    private synchronized void doExport() {
        if (isExposed()) {
            return;
        }
        setExposed(true);
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<fang:service interface=\"\" /> interface not allow null!");
        }
        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        checkInterfaceAndMethods(interfaceClass);
        checkRef();
        doExportUrls();

    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        fullConfig();
        if (!isDelay()) {
            export();
        }
    }


    private void doExportUrls() {

        URL registryURL = loadNodes();
        openServe(registryURL);
        //TODO 获得本地ip，和host；或者根据配置文件中的ip后进行注册，
        if (!Objects.isNull(registryURL)) {
            ServiceProvider provider = new ServiceProviderImpl(registryURL);
            scheduledExecutorService.execute(() -> {
                provider.publishService(registryURL, interfaceClass, ref);
            });
        }

    }

    private void openServe(URL config) {
        Boolean isServe = Boolean.valueOf(config.getParameter(Constants.IS_SERVER, Boolean.FALSE.toString()));
        if (!isServe) {
            doOpen(config);
        }
    }

    private void doOpen(URL config) {
        this.isOpen = true;
        RpcServer nettyRpcServer = ExtensionLoader
                .getExtension(RpcServer.class, config.getParameter(Constants.TRANSPORT, Constants.TRANSPORT_DEFAULT));
        nettyRpcServer.start(config);
    }


    private boolean isDelay() {
        Integer delay = getDelay();
        return supportedApplicationListener && (delay == null || delay == -1);
    }


}
