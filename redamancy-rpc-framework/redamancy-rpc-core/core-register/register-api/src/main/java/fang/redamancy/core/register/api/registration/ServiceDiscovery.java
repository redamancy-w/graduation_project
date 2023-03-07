package fang.redamancy.core.register.api.registration;

import fang.redamancy.core.common.extension.SPI;

import java.net.URL;

/**
 * @Author redamancy
 * @Date 2023/1/17 19:51
 * @Version 1.0
 */
@SPI("nacos")
public interface ServiceDiscovery {

    void getRegistry(URL url);

}
