package fang.redamancy.core.config.spring.annotation;

import fang.redamancy.core.config.spring.annotation.config.FangRpcConfig;
import fang.redamancy.core.config.spring.annotation.server.FangComponentScan;

import java.lang.annotation.*;

/**
 * @Author redamancy
 * @Date 2023/2/19 16:36
 * @Version 1.0
 */


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@FangRpcConfig
@FangComponentScan
public @interface EnableFangRpc {
}
