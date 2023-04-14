package fang.redamancy.core.config.support;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fang.redamancy.core.common.annotation.FangReference;
import fang.redamancy.core.common.annotation.FangService;
import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.common.util.ConfigUtil;
import fang.redamancy.core.common.util.RuntimeUtil;
import fang.redamancy.core.config.support.model.FangNodeConfig;
import fang.redamancy.core.config.support.model.FangRegistryConfig;
import fang.redamancy.core.config.util.SpringApplicationContextPool;
import fang.redamancy.core.provide.ServiceProvider;
import fang.redamancy.core.provide.support.Impl.ServiceProviderImpl;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.imageio.spi.ServiceRegistry;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author redamancy
 * @Date 2023/3/3 17:12
 * @Version 1.0
 */
@Setter
@Getter
public class AbstractServiceConfig<T> extends ServiceConfig implements ApplicationContextAware, DisposableBean {

    private static final long serialVersionUID = 43873823728L;

    /**
     * springioc 上下文
     */
    protected static transient ApplicationContext SPRING_CONTEXT;

    /**
     * referencebean是否初始化
     */
    protected transient volatile boolean init;

    protected transient boolean supportedApplicationListener;


    protected transient ApplicationContext applicationContext;

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(RuntimeUtil.cpus(),
            new ThreadFactoryBuilder()
                    .setNameFormat("Exposed-Service-pool-")
                    .setDaemon(true)
                    .build()
    );

    /**
     * id
     * 如果没设置默认为接口名
     */
    protected String id;


    /**
     *
     */
    protected T ref;
    protected String group;
    protected String version;
    protected String host;
    protected Integer port;
    private List<FangNodeConfig> nodeConfigs;
    private FangRegistryConfig registryConfig;
    protected Integer delay;

    protected String beanName;

    /**
     * 是否已暴露
     */
    private volatile boolean isExposed;

    /**
     * 实现的接口
     */
    protected String interfaceName;

    protected Class<?> interfaceClass;

    public synchronized void export() {

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


    protected synchronized void doExport() {
        if (isExposed) {
            return;
        }
        isExposed = true;
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

    protected Class<?> getInterfaceClass() {

        if (interfaceClass != null) {
            return interfaceClass;
        }

        try {

            if (interfaceName != null && interfaceName.length() > 0) {
                this.interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                        .getContextClassLoader());
            }

        } catch (ClassNotFoundException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return interfaceClass;
    }

    private void doExportUrls() {

        List<URL> registryURLs = loadNodes();

        Map<String, String> map = new HashMap<String, String>();

        if (ConfigUtil.getPid() > 0) {
            // 设置
            map.put(Constants.PID_KEY, String.valueOf(ConfigUtil.getPid()));
        }

        //TODO 获得本地ip，和host；或者根据配置文件中的ip后进行注册，

        if (!CollectionUtils.isEmpty(registryURLs)) {

            for (URL registryURL : registryURLs) {

                ServiceProvider provider = new ServiceProviderImpl(registryURL);

                scheduledExecutorService.execute(() -> {
                    provider.publishService(registryURL, interfaceClass);
                });
            }

        }

    }

    private List<URL> loadNodes() {

        checkNode();

        List<URL> nodeList = new ArrayList<URL>();

        for (FangNodeConfig nodeConfig : nodeConfigs) {
            String address = nodeConfig.getAddress();

            if (!StringUtils.hasText(address)) {
                address = Constants.ANYHOST_VALUE;
            }

            if (StringUtils.hasText(address)) {
                Map<String, String> map = new HashMap<String, String>();
                appendParameters(map, registryConfig);
                appendParameters(map, nodeConfig);

                map.put("path", ServiceRegistry.class.getName());
                map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
                if (ConfigUtil.getPid() > 0) {
                    map.put(Constants.PID_KEY, String.valueOf(ConfigUtil.getPid()));
                }

                URL url = ConfigUtil.parseURL(address, map);

                nodeList.add(url);
            }
        }

        return nodeList;
    }

    private void checkNode() {
        if (CollectionUtils.isEmpty(nodeConfigs)) {
            throw new IllegalStateException(
                    "未检测到配置的注册中心节点信息");
        }
    }

    private void checkRef() {
        if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }

        if (!interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class "
                    + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }

    }

    private void checkInterfaceAndMethods(Class<?> interfaceClass) {

        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }

        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }

    }


    public AbstractServiceConfig(FangService service) {
        appendAnnotation(FangService.class, service);
    }

    public AbstractServiceConfig(FangReference reference) {
        appendAnnotation(FangReference.class, reference);
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (!StringUtils.hasText(id)) {
            this.id = interfaceName;
        }
    }

    public String getInterface() {
        return this.interfaceName;
    }


    protected void fullConfig() {

        if (CollectionUtils.isEmpty(getNodeConfigs())) {

            Map<String, FangNodeConfig> fangNodeConfigMap = applicationContext == null ? null
                    : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, FangNodeConfig.class, false, false);
            if (fangNodeConfigMap != null && fangNodeConfigMap.size() > 0) {

                List<FangNodeConfig> nodeConfigs = new ArrayList<FangNodeConfig>();

                for (FangNodeConfig config : fangNodeConfigMap.values()) {
                    if (config.getIsDefault() == null || config.getIsDefault().booleanValue()) {
                        nodeConfigs.add(config);
                    }
                }
                if (!CollectionUtils.isEmpty(nodeConfigs)) {
                    setNodeConfigs(nodeConfigs);
                }
            }
        }


        //TODO 注册信息类的注入
        if (getRegistryConfig() == null) {

            Map<String, FangNodeConfig> fangNodeConfigMap = applicationContext == null ? null
                    : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, FangNodeConfig.class, false, false);
            if (fangNodeConfigMap != null && fangNodeConfigMap.size() > 0) {

                FangNodeConfig fangNodeConfig = null;
                for (FangNodeConfig config : fangNodeConfigMap.values()) {
                    if (config.getIsDefault() == null || config.getIsDefault().booleanValue()) {

                        if (fangNodeConfig != null) {
                            throw new IllegalStateException("有重复的配置: " + fangNodeConfig + " and " + config);
                        }
                        fangNodeConfig = config;
                    }
                }
                if (fangNodeConfig != null) {

                }

            }
        }
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        SpringApplicationContextPool.addApplicationContext(applicationContext);
        SPRING_CONTEXT = applicationContext;

        try {

            //向后兼容spring
            Method method = applicationContext.getClass().getMethod("addApplicationListener", new Class<?>[]{ApplicationListener.class});
            method.invoke(applicationContext, new Object[]{this});
            supportedApplicationListener = true;

        } catch (Throwable t) {

            if (applicationContext instanceof AbstractApplicationContext) {

                try {
                    //向后兼容
                    Method method = AbstractApplicationContext.class.getDeclaredMethod("addListener", new Class<?>[]{ApplicationListener.class});

                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }

                    method.invoke(applicationContext, new Object[]{this});
                    supportedApplicationListener = true;
                } catch (Throwable t2) {

                }

            }

        }
    }

    @Override
    public void destroy() throws Exception {

    }
}
