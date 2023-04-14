package fang.redamancy.core.config.support.service;

import fang.redamancy.core.common.annotation.FangService;
import fang.redamancy.core.config.support.AbstractServiceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

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
public class ApplicationListenerRegistrar<T> extends AbstractServiceConfig<T> implements InitializingBean, ApplicationListener<ContextRefreshedEvent>, BeanNameAware {


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

    private boolean isDelay() {
        Integer delay = getDelay();
        return supportedApplicationListener && (delay == null || delay == -1);
    }


}
