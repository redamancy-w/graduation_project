package fang.redamancy.core.config.spring.annotation.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author redamancy
 * @Date 2023/2/19 16:37
 * @Version 1.0
 */


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(FangConfigConfigurationSelector.class)
public @interface FangRpcConfig {
}
