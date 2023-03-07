package fang.redamancy.core.config.spring.annotation.config;

import fang.redamancy.core.config.spring.annotation.config.ConfigBindingRegistrar;
import fang.redamancy.core.config.spring.annotation.config.ConfigBindings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

/**
 * 绑定注册器
 *
 * @Author redamancy
 * @Date 2023/2/20 09:32
 * @Version 1.0
 */
public class ConfigBindingsRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private ConfigurableEnvironment environment;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(ConfigBindings.class.getName()));

        AnnotationAttributes[] annotationAttributes = attributes.getAnnotationArray("value");
        ConfigBindingRegistrar registrar            = new ConfigBindingRegistrar();

        registrar.setEnvironment(environment);
        for (AnnotationAttributes element : annotationAttributes) {
            registrar.registerBeanDefinitions(element, registry);
        }
    }

    @Override
    public void setEnvironment(@NotNull Environment environment) {

        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        this.environment = (ConfigurableEnvironment) environment;
    }
}
