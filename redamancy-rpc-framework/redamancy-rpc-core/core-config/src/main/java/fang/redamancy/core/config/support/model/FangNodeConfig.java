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
public class FangNodeConfig extends ServiceConfig {


    /**
     * 运输
     */
    private String transport;

    /**
     * 是默认
     */
    private Boolean isDefault;

    /**
     * 绑定端口
     */
    private Integer bindPort;

    /**
     * 是服务
     */
    private Boolean isServe;

    /**
     * 压缩
     */
    private String compress;

    /**
     * 序列化
     */
    private String serialize;

    private String loadBalance;

    private Boolean shortConnection;

}
