package fang.redamancy.core.common.extension;

import java.lang.annotation.*;

/**
 * @Author redamancy
 * @Date 2023/1/8 16:35
 * @Version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * 默认实现
     */
    String value() default "";
}
