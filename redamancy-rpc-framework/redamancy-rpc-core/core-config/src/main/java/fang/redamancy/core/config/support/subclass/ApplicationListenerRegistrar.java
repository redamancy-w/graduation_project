package fang.redamancy.core.config.support.subclass;

import fang.redamancy.core.common.annotation.FangService;
import fang.redamancy.core.config.Application;
import fang.redamancy.core.config.support.AbstractServiceConfig;
import fang.redamancy.core.config.util.SpringApplicationContextPool;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author redamancy
 * @Date 2023/3/3 13:35
 * @Version 1.0
 */
@Setter
@Getter
@Slf4j
public class ApplicationListenerRegistrar<T> extends AbstractServiceConfig<T> implements InitializingBean, DisposableBean, ApplicationListener<ContextRefreshedEvent>, BeanNameAware, ApplicationContextAware {

    /**
     * springioc 上下文
     */
    private static transient ApplicationContext SPRING_CONTEXT;


    private transient ApplicationContext applicationContext;

    private transient boolean supportedApplicationListener;


    public ApplicationListenerRegistrar(FangService service) {
        super(service);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("The service ready on spring started. service: " + getInterface());

    }

    @Override
    public void setBeanName(String s) {
    }

    @Override
    public void destroy() throws Exception {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
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
                if (CollectionUtils.isEmpty(nodeConfigs)) {
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

        if (!isDelay()) {
            export();
        }
    }

    private boolean isDelay() {
        Integer delay = getDelay();
        return supportedApplicationListener && (delay == null || delay == -1);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        SpringApplicationContextPool.addApplicationContext(applicationContext);

        SPRING_CONTEXT = applicationContext;

        try {

            // backward compatibility to spring 2.0.1
            Method method = applicationContext.getClass().getMethod("addApplicationListener", new Class<?>[]{ApplicationListener.class});
            method.invoke(applicationContext, new Object[]{this});
            supportedApplicationListener = true;
        } catch (Throwable t) {
            if (applicationContext instanceof AbstractApplicationContext) {

                try {
                    // backward compatibility to spring 2.0.1
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

}
