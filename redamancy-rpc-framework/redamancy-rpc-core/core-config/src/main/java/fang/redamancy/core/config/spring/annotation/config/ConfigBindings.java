package fang.redamancy.core.config.spring.annotation.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author redamancy
 * @Date 2023/2/20 09:28
 * @Version 1.0
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigBindingsRegistrar.class)
public @interface ConfigBindings {


    /**
     * @return non-null
     */
    ConfigBinding[] value();
}
