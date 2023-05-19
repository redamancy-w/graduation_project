package fang.redamancy.core.common.annotation;

import java.lang.annotation.*;

/**
 * @Author redamancy
 * @Date 2023/2/27 16:26
 * @Version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface FangReference {


    /**
     * 版本
     *
     * @return
     */
    String version() default "";

    /**
     * 组
     *
     * @return
     */
    String group() default "";


    /**
     * 超时
     */
    String timeout() default "5000";

}

