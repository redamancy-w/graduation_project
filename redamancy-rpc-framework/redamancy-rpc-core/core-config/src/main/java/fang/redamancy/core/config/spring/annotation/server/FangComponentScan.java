package fang.redamancy.core.config.spring.annotation.server;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author redamancy
 * @Date 2023/2/27 14:34
 * @Version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(FangComponentScanRegister.class)
public @interface FangComponentScan {


    /**
     * 扫描包的地址
     *
     * @return
     */
    String[] value() default {};

    /**
     * 扫描接口的包地址
     */
    String[] basePackages() default {};

    /**
     * 手动添加接口类
     */
    Class<?>[] basePackageClasses() default {};

}
