package fang.redamancy.core.config.spring.annotation.config;

import fang.redamancy.core.config.spring.annotation.processor.ConfigBindingBeanPostProcessor;
import fang.redamancy.core.config.support.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerWithGeneratedName;

/**
 * @Author redamancy
 * @Date 2023/2/20 09:38
 * @Version 1.0
 */
@Slf4j
public class ConfigBindingRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        this.environment = (ConfigurableEnvironment) environment;
    }


    protected void registerBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {

        String prefix = environment.resolvePlaceholders(attributes.getString("prefix"));

        Class<? extends ServiceConfig> configClass = attributes.getClass("type");

        registerConfigBean(prefix, configClass, registry);
    }

    private void registerConfigBean(String prefix, Class<? extends ServiceConfig> type, BeanDefinitionRegistry registry) {
        PropertySources propertySources = environment.getPropertySources();
        Map<String, String> properties = getSubProperties(propertySources, prefix);

        if (CollectionUtils.isEmpty(properties)) {
            log.debug("There is no property for binding to dubbo config class [" + type.getName()
                    + "] within prefix [" + prefix + "]");
            return;
        }
        Set<String> beanNames = Collections.singleton(resolveSingleBeanName(type, properties, registry));

        beanNames.forEach(beanName -> {

            registerDubboConfigBean(beanName, type, registry);

            MutablePropertyValues propertyValues = resolveBeanPropertyValues(properties);

            registerConfigBindingBeanPostProcessor(beanName, propertyValues, registry);

        });

    }


    /**
     * @param beanName
     * @param configClass
     * @param registry
     */
    private void registerDubboConfigBean(String beanName, Class<? extends ServiceConfig> configClass,
                                         BeanDefinitionRegistry registry) {

        BeanDefinitionBuilder builder = rootBeanDefinition(configClass);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

        registry.registerBeanDefinition(beanName, beanDefinition);

        log.info("配置类Bean name : " + beanName + ", class : " + configClass.getName() +
                " 注册成功");
    }

    /**
     * 将prefix前缀的配置设进行映射
     *
     * @param propertySources
     * @param prefix
     * @return
     */
    private static Map<String, String> getSubProperties(PropertySources propertySources, String prefix) {

        Map<String, String> subProperties = new LinkedHashMap<String, String>();
        String normalizedPrefix = prefix.endsWith(".") ? prefix : prefix + ".";

        for (PropertySource<?> source : propertySources) {
            if (source instanceof EnumerablePropertySource) {
                for (String name : ((EnumerablePropertySource<?>) source).getPropertyNames()) {
                    if (name.startsWith(normalizedPrefix)) {
                        String subName = name.substring(normalizedPrefix.length());
                        Object value = source.getProperty(name);
                        subProperties.put(subName, String.valueOf(value));
                    }
                }
            }
        }
        return subProperties;
    }


    /**
     * 构建Bean的Name
     *
     * @param configClass
     * @param properties
     * @param registry
     * @return
     */
    private String resolveSingleBeanName(Class<? extends ServiceConfig> configClass, Map<String, String> properties,
                                         BeanDefinitionRegistry registry) {

        String beanName = properties.get("id");

        if (!StringUtils.hasText(beanName)) {
            BeanDefinitionBuilder builder = rootBeanDefinition(configClass);
            beanName = BeanDefinitionReaderUtils.generateBeanName(builder.getRawBeanDefinition(), registry);
        }

        return beanName;

    }


    /**
     * 获取配置文件中的配置值，并返回到Mutable中
     *
     * @param properties
     * @return
     */
    private MutablePropertyValues resolveBeanPropertyValues(
            Map<String, String> properties) {

        MutablePropertyValues propertyValues = new MutablePropertyValues();


        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String propertyName = entry.getKey();

            // 去除携带的 "."
            if (!propertyName.contains(".")) {
                propertyValues.addPropertyValue(propertyName, entry.getValue());
            }
        }

        return propertyValues;
    }

    /**
     * 将类名和属性值放到spring的后置处理器中，并将后置处理器注入到ioc中，
     *
     * @param beanName
     * @param propertyValues
     * @param registry
     */
    private void registerConfigBindingBeanPostProcessor(String beanName, PropertyValues propertyValues,
                                                        BeanDefinitionRegistry registry) {

        Class<?> processorClass = ConfigBindingBeanPostProcessor.class;

        BeanDefinitionBuilder builder = rootBeanDefinition(processorClass);
        builder.addConstructorArgValue(beanName).addConstructorArgValue(propertyValues);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registerWithGeneratedName(beanDefinition, registry);

        log.info("The BeanPostProcessor bean definition [" + processorClass.getName()
                + "] for dubbo config bean [name : " + beanName + "] has been registered.");


    }
}
