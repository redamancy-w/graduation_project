package fang.redamancy.core.config.support;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fang.redamancy.core.common.annotation.FangService;
import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.common.util.ConfigUtil;
import fang.redamancy.core.common.util.RuntimeUtil;
import fang.redamancy.core.config.support.subclass.FangNodeConfig;
import fang.redamancy.core.config.support.subclass.FangRegistryConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.imageio.spi.ServiceRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author redamancy
 * @Date 2023/3/3 17:12
 * @Version 1.0
 */
@Setter
@Getter
public class AbstractServiceConfig<T> extends ServiceConfig {
    private static final long serialVersionUID = 43873823728L;


    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(RuntimeUtil.cpus(),
            new ThreadFactoryBuilder()
                    .setNameFormat("Exposed-Service-pool-")
                    .setDaemon(true)
                    .build()
    );

    /**
     * id
     * 如果没设置默认为接口名
     */
    protected String id;

    /**
     *
     */
    protected T                    ref;
    protected String               group;
    protected String               version;
    protected String               host;
    protected Integer              port;
    private   List<FangNodeConfig> nodeConfigs;
    private   FangRegistryConfig   registryConfig;

    protected Integer delay;

    /**
     * 是否已暴露
     */
    private Boolean isExposed;

    /**
     * 实现的接口
     */
    private String interfaceName;

    private Class<?> interfaceClass;

    public synchronized void export() {
        if (isExposed != null && !isExposed) {
            return;
        }
        if (delay != null && delay > 0) {
            scheduledExecutorService.schedule(new Runnable() {
                public void run() {
                    doExport();
                }
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            doExport();
        }

    }


    protected synchronized void doExport() {
        if (isExposed) {
            return;
        }
        isExposed = true;
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<fang:service interface=\"\" /> interface not allow null!");
        }

        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        checkInterfaceAndMethods(interfaceClass);
        checkRef();
        doExportUrls();


    }

    private void doExportUrls() {

        List<URL> registryURLs = loadNodes();

        Map<String, String> map = new HashMap<String, String>();

        if (ConfigUtil.getPid() > 0) {
            // 设置
            map.put(Constants.PID_KEY, String.valueOf(ConfigUtil.getPid()));
        }
        appendParameters(map, registryConfig);
        //TODO 获得本地ip，和host；或者根据配置文件中的ip后进行注册，


    }


    private List<URL> loadNodes() {
        checkNode();
        List<URL> nodeList = new ArrayList<URL>();

        for (FangNodeConfig nodeConfig : nodeConfigs) {
            String address = nodeConfig.getAddress();

            if (!StringUtils.hasText(address)) {
                address = Constants.ANYHOST_VALUE;
            }

            if (!StringUtils.hasText(address)) {
                Map<String, String> map = new HashMap<String, String>();
                appendParameters(map, registryConfig);
                appendParameters(map, nodeConfig);

                map.put("path", ServiceRegistry.class.getName());
                map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
                if (ConfigUtil.getPid() > 0) {
                    map.put(Constants.PID_KEY, String.valueOf(ConfigUtil.getPid()));
                }

                URL url = ConfigUtil.parseURL(address, map);

                nodeList.add(url);
            }
        }

        return nodeList;
    }

    private void checkNode() {
        if (CollectionUtils.isEmpty(nodeConfigs)) {
            throw new IllegalStateException(
                    "未检测到配置的注册中心节点信息");
        }
    }

    private void checkRef() {
        if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }

        if (!interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class "
                    + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }

    }

    private void checkInterfaceAndMethods(Class<?> interfaceClass) {

        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }

        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }

    }


    public AbstractServiceConfig(FangService service) {
        appendAnnotation(FangService.class, service);
    }

    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (!StringUtils.hasText(id)) {
            this.id = interfaceName;
        }
    }

    public String getInterface() {
        return this.interfaceName;
    }
}
