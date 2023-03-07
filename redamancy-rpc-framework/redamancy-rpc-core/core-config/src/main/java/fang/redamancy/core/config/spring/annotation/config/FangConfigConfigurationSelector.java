package fang.redamancy.core.config.spring.annotation.config;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Author redamancy
 * @Date 2023/2/19 16:39
 * @Version 1.0
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
