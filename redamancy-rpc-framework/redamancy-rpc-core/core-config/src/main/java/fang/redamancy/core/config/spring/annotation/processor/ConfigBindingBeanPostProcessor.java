package fang.redamancy.core.config.spring.annotation.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.validation.DataBinder;

import java.util.Arrays;

/**
 * @Author redamancy
 * @Date 2023/2/21 16:31
 * @Version 1.0
 */
@Slf4j
public class ConfigBindingBeanPostProcessor implements BeanPostProcessor {

    /**
     * 类名
     */
    private final String beanName;

    /**
     * 属性名
     */
    private final PropertyValues propertyValues;

    public ConfigBindingBeanPostProcessor(String beanName, PropertyValues propertyValues) {
        this.beanName = beanName;
        this.propertyValues = propertyValues;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if (beanName.equals(this.beanName)) {
            DataBinder dataBinder = new DataBinder(bean);
            dataBinder.setIgnoreInvalidFields(true);
            dataBinder.bind(propertyValues);
            log.info("类名为 name : " + beanName + "] 将属性值绑定到bean中 : "
                    + Arrays.asList(propertyValues.getPropertyValues()));
        }
        return bean;
    }
}

