package fang.redamancy.core.config.support.model;

import fang.redamancy.core.config.support.ServiceConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author redamancy
 * @Date 2023/2/20 10:33
 * @Version 1.0
 */
@Getter
@Setter
public class FangRegistryConfig extends ServiceConfig {

    private String transport;

    private Boolean isDefault;

    private Integer bindPort;

    private Boolean isServe;

}
