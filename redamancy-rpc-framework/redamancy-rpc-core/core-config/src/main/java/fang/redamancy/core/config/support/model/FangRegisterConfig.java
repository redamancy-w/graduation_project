package fang.redamancy.core.config.support.model;

import fang.redamancy.core.config.support.ServiceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @Author redamancy
 * @Date 2023/2/20 10:32
 * @Version 1.0
 */
@Slf4j
@Setter
@Getter
public class FangRegisterConfig extends ServiceConfig {

    private static final long serialVersionUID = 23232L;

    private String address;

    /**
     * 协议名
     * "nacos"
     * "redis"
     * "zookeeper"
     */
    private String name;

    private String host;

    private Integer port;

    private String username;

    private String password;

    private Boolean isDefault;

    private Map<String, String> parameters;

    private String namespace;

}
