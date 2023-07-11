package fang.redamancy.core.config.spring.annotation.config;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Author redamancy
 * @Date 2023/2/19 16:39
 * @Version 1.0
 * 这个方法的返回值是一个字符串数组，只要在配置类被引用了，
 * 这里返回的字符串数组中的类名就会被Spring容器new出来，然后再把这些对象放到工厂当中去。所以这有啥用呢？我们还是用一个例子演示一下。
 */
public class FangConfigConfigurationSelector implements ImportSelector, Ordered {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return of(FangConfigConfiguration.Single.class.getName());
    }

    private static <T> T[] of(T... values) {
        return values;
    }


    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
