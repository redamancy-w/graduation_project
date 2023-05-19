package fang.redamancy.core.config.support;

import fang.redamancy.core.common.annotation.FangReference;
import fang.redamancy.core.common.annotation.FangService;
import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.util.ConfigUtil;
import fang.redamancy.core.common.util.NetUtil;
import fang.redamancy.core.config.support.model.FangNodeConfig;
import fang.redamancy.core.config.support.model.FangRegisterConfig;
import fang.redamancy.core.config.util.SpringApplicationContextPool;
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
import org.springframework.util.StringUtils;

import javax.imageio.spi.ServiceRegistry;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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


    protected static Map<String, String> cacheMap = new HashMap<>();

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
    protected String timeout;
    protected String host;
    protected Integer port;
    protected FangRegisterConfig nodeConfig;
    protected FangNodeConfig registryConfig;
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

    public AbstractServiceConfig(FangService service) {
        appendAnnotation(FangService.class, service);
    }

    public AbstractServiceConfig(FangReference reference) {
        appendAnnotation(FangReference.class, reference);
    }

    public AbstractServiceConfig() {
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


    protected RpcConfig loadNodes() {

        checkNode();
        RpcConfig rpcConfig = null;

        String address = nodeConfig.getAddress();

        if (!StringUtils.hasText(address)) {
            address = Constants.ANYHOST_VALUE;
        }

        if (!StringUtils.hasText(address)) {
            throw new IllegalStateException("未配置注册中心地址信息");
        }

        Map<String, String> map = new HashMap<String, String>();
        appendParameters(map, registryConfig);
        appendParameters(map, nodeConfig);

        map.put("path", ServiceRegistry.class.getName());
        map.put(Constants.INTERFACE_KEY, interfaceName);
        map.put(Constants.VERSION_KEY, version);
        map.put(Constants.GROUP_KEY, group);
        map.put(Constants.TIMEOUT_KEY, timeout);
        map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        map.put(Constants.BIND_PORT, getBindPort());

        if (ConfigUtil.getPid() > 0) {
            map.put(Constants.PID_KEY, String.valueOf(ConfigUtil.getPid()));
        }

        rpcConfig = ConfigUtil.parseURL(address, map);

        return rpcConfig;
    }


    private void checkNode() {
        if (Objects.isNull(nodeConfig)) {
            throw new IllegalStateException(
                    "未检测到配置的注册中心节点信息");
        }
    }

    protected void checkRef() {
        if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }

        if (!interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class "
                    + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }

    }

    protected void checkInterfaceAndMethods(Class<?> interfaceClass) {

        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }

        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }

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

        if (Objects.isNull(getNodeConfig())) {

            Map<String, FangRegisterConfig> fangNodeConfigMap = applicationContext == null ? null
                    : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, FangRegisterConfig.class, false, false);
            if (fangNodeConfigMap != null && fangNodeConfigMap.size() > 0) {

                FangRegisterConfig nodeConfigs = null;

                for (FangRegisterConfig config : fangNodeConfigMap.values()) {
                    if (config.getIsDefault() == null || config.getIsDefault().booleanValue()) {
                        if (nodeConfigs != null) {
                            throw new IllegalStateException("有重复的配置: " + nodeConfigs + " and " + config);
                        }
                        nodeConfigs = config;
                    }
                }

                if (!Objects.isNull(nodeConfigs)) {
                    setNodeConfig(nodeConfigs);
                }
            }
        }


        //TODO 注册信息类的注入
        if (getRegistryConfig() == null) {

            Map<String, FangNodeConfig> fangRegistryConfigMap = applicationContext == null ? null
                    : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, FangNodeConfig.class, false, false);
            if (fangRegistryConfigMap != null && fangRegistryConfigMap.size() > 0) {

                FangNodeConfig fangNodeConfig = null;

                for (FangNodeConfig config : fangRegistryConfigMap.values()) {

                    if (config.getIsDefault() == null || config.getIsDefault().booleanValue()) {

                        if (fangNodeConfig != null) {
                            throw new IllegalStateException("有重复的配置: " + fangNodeConfig + " and " + config);
                        }
                        fangNodeConfig = config;
                    }
                }

                if (fangNodeConfig != null) {
                    setRegistryConfig(fangNodeConfig);
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

    private String getBindPort() {
        Integer portToBind = null;

        if (cacheMap.containsKey(Constants.BIND_PORT)) {
            return cacheMap.get(Constants.BIND_PORT);
        }

        portToBind = registryConfig.getBindPort();


        if (portToBind == null) {

            portToBind = Constants.BIND_PORT_DEFAULT;
        }

        if (portToBind <= 0) {
            portToBind = NetUtil.getAvailablePort();
        }

        cacheMap.put(Constants.BIND_PORT, String.valueOf(portToBind));
        return String.valueOf(portToBind);
    }

    @Override
    public void destroy() throws Exception {
    }
}
