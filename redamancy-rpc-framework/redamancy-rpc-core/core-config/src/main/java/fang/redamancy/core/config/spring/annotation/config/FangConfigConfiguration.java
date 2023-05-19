package fang.redamancy.core.config.spring.annotation.config;

import fang.redamancy.core.config.support.model.FangNodeConfig;
import fang.redamancy.core.config.support.model.FangRegisterConfig;

/**
 * @Author redamancy
 * @Date 2023/2/19 16:52
 * @Version 1.0
 */
public class FangConfigConfiguration {
    @ConfigBindings({
            @ConfigBinding(prefix = "fang.register", type = FangRegisterConfig.class),
            @ConfigBinding(prefix = "fang.node", type = FangNodeConfig.class)
    })
    public static class Single {
    }
}