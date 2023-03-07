package fang.redamancy.core.config.spring.annotation.server;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

import static org.springframework.context.annotation.AnnotationConfigUtils.registerAnnotationConfigProcessors;

/**
 * @Author redamancy
 * @Date 2023/2/27 16:21
 * @Version 1.0
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {
    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> anooType, Environment environment,
                         ResourceLoader resourceLoader) {

        super(registry);

        //添加过滤器,过滤出带有@FangService的类
        super.addIncludeFilter(new AnnotationTypeFilter(anooType));

        setEnvironment(environment);
        setResourceLoader(resourceLoader);

        registerAnnotationConfigProcessors(registry);
    }


    public boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        return super.checkCandidate(beanName, beanDefinition);
    }


    @Override
    public int scan(String... basePackages) {
        return super.scan(basePackages);
    }
}
