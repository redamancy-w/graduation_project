package fang.redamancy.core.register.api.registration;

import fang.redamancy.core.common.extension.SPI;
import fang.redamancy.core.common.net.support.URL;

import java.util.List;


/**
 * @Author redamancy
 * @Date 2023/1/17 19:50
 * @Version 1.0
 */
@SPI("nacos")
public interface ServiceRegistry {

    void register(URL url);

    void unregister(URL url);

    List<URL> discoverRegister(String serviceKey);
}
