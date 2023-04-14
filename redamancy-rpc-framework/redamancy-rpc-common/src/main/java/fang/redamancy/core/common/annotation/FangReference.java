package fang.redamancy.core.common.annotation;

/**
 * @Author redamancy
 * @Date 2023/2/27 16:26
 * @Version 1.0
 */
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
}
