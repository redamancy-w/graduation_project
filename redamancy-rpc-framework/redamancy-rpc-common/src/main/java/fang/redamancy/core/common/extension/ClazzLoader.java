package fang.redamancy.core.common.extension;

import cn.hutool.core.util.StrUtil;
import fang.redamancy.core.common.model.ExtensionYaml;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @Author redamancy
 * @Date 2023/1/9 18:15
 * @Version 1.0
 */
@Slf4j
public class ClazzLoader {

    /**
     * 配置文件目录
     */
    private static final String SERVICES_DIRECTORY = "META-INF/services/fang.yaml";

    public void loadDirectory(Map<String, Class<?>> extensionClasses, Class<?> className) {

        if (StrUtil.hasEmpty(className.getName())) {
            throw new IllegalArgumentException("类名为空");
        }

        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();

            urls = classLoader.getResources(SERVICES_DIRECTORY);

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl, className.getName());
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl, String className) {

        try (InputStream inputStream = Files.newInputStream(Paths.get(resourceUrl.getFile()))) {
            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yaml = new Yaml(representer);

            ExtensionYaml extensionYaml = yaml.loadAs(inputStream, ExtensionYaml.class);

            Map<String, String> classMap = extensionYaml.getClassesMap(className);
            
            for (Entry<String, String> entry : classMap.entrySet()) {
                String name = entry.getKey();
                String clazzName = entry.getValue();

                if (!StrUtil.hasEmpty(name, className)) {

                    try {
                        Class<?> clazz = Class.forName(clazzName, true, classLoader);
                        extensionClasses.put(name, clazz);
                    } catch (ClassNotFoundException exception) {
                        log.error("找不到所查询的类,类名为:{},", clazzName);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
