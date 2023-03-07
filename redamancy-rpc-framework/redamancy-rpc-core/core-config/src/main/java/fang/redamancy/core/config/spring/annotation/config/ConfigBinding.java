package fang.redamancy.core.config.spring.annotation.config;

import fang.redamancy.core.config.support.ServiceConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author redamancy
 * @Date 2023/2/20 09:29
 * @Version 1.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigBindingRegistrar.class)
public @interface ConfigBinding {

    /**
     * 配置文件中的前缀
     *
     * @return prefix
     */
    String prefix();

    /**
     * 绑定的类型
     */
    Class<? extends ServiceConfig> type();

}
