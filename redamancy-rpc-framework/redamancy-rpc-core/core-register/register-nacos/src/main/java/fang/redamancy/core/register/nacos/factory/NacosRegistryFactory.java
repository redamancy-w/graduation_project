package fang.redamancy.core.register.nacos.factory;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.common.utils.StringUtils;
import fang.redamancy.core.common.model.RpcConfig;
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
    protected Register buildRegisterClient(RpcConfig rpcConfig) {
        return new NacosRegistry(createNamingService(rpcConfig), rpcConfig);
    }

    private NamingService createNamingService(RpcConfig rpcConfig) {
        Properties properties = fillProperties(rpcConfig);

        NamingService namingService = null;
        try {
            namingService = NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error("构建Naming失败,注册中心信息{}", rpcConfig);
            throw new RuntimeException(e);
        }


        return namingService;
    }

    private Properties fillProperties(RpcConfig rpcConfig) {
        Properties properties = new Properties();

        String serverAddr = rpcConfig.getHost();
        //客户端地址
        properties.put(SERVER_ADDR, serverAddr);
        //命名空间
        putPropertyIfAbsent(rpcConfig, properties, NAMESPACE);
        //日志目录
        putPropertyIfAbsent(rpcConfig, properties, NACOS_NAMING_LOG_NAME);
        //连接Nacos Server指定的连接点
        putPropertyIfAbsent(rpcConfig, properties, ENDPOINT);
        //鉴权
        putPropertyIfAbsent(rpcConfig, properties, USERNAME);
        //鉴权
        putPropertyIfAbsent(rpcConfig, properties, PASSWORD);
        //集群名
        putPropertyIfAbsent(rpcConfig, properties, CLUSTER_NAME);

        return properties;
    }


    private void putPropertyIfAbsent(RpcConfig rpcConfig, Properties properties, String propertyName) {
        String propertyValue = rpcConfig.getParameter(propertyName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            properties.setProperty(propertyName, propertyValue);
        }
    }
}
