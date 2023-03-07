package fang.redamancy.core.register.nacos.factory;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.common.utils.StringUtils;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.register.api.factory.support.AbstractRegisterFactory;
import fang.redamancy.core.register.api.registration.Register;
import fang.redamancy.core.register.nacos.register.NacosRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.*;
import static com.alibaba.nacos.client.naming.utils.UtilAndComs.NACOS_NAMING_LOG_NAME;

/**
 * @Author redamancy
 * @Date 2023/2/4 20:10
 * @Version 1.0
 */
@Slf4j
public class NacosRegistryFactory extends AbstractRegisterFactory {

    @Override
    protected Register buildRegisterClient(URL url) {
        return new NacosRegistry(createNamingService(url), url);
    }

    private NamingService createNamingService(URL url) {
        Properties properties = fillProperties(url);

        NamingService namingService = null;
        try {
            namingService = NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error("构建Naming失败,注册中心信息{}", url);
            throw new RuntimeException(e);
        }
        return namingService;
    }

    private Properties fillProperties(URL url) {
        Properties properties = new Properties();

        String serverAddr = url.getHost();
        //客户端地址
        properties.put(SERVER_ADDR, serverAddr);
        //命名空间
        putPropertyIfAbsent(url, properties, NAMESPACE);
        //日志目录
        putPropertyIfAbsent(url, properties, NACOS_NAMING_LOG_NAME);
        //连接Nacos Server指定的连接点
        putPropertyIfAbsent(url, properties, ENDPOINT);
        //鉴权
        putPropertyIfAbsent(url, properties, USERNAME);
        //鉴权
        putPropertyIfAbsent(url, properties, PASSWORD);
        //集群名
        putPropertyIfAbsent(url, properties, CLUSTER_NAME);

        return properties;
    }

    private void putPropertyIfAbsent(URL url, Properties properties, String propertyName) {
        String propertyValue = url.getParameter(propertyName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            properties.setProperty(propertyName, propertyValue);
        }
    }
}
