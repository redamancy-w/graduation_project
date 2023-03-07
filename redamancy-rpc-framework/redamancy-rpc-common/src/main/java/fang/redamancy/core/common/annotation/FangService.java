package fang.redamancy.core.common.annotation;

import java.lang.annotation.*;

/**
 * @Author redamancy
 * @Date 2023/2/27 16:26
 * @Version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface FangService {

    String version() default "";

    String group() default "";

}
