package fang.redamancy.core.config.spring.annotation.server;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @Author redamancy
 * @Date 2023/2/27 17:09
 * @Version 1.0
 */
public class ComponentScanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }


}
