package fang.redamancy.core.common.extension;

import cn.hutool.core.util.StrUtil;
import fang.redamancy.core.common.enums.ExtensionLoaderSupportEnum;
import fang.redamancy.core.common.util.Holder;
import fang.redamancy.core.common.util.SingletonFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @Author redamancy
 * @Date 2023/1/8 16:35
 * @Version 1.0
 */
@Slf4j
public class ExtensionLoader<T> {


    /**
     * ExtensionLoader 缓存
     */
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    /**
     * 缓存已有的实例
     */
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();
    /**
     * 该ExtensionLoader的类型
     */
    private final Class<?> type;
    /**
     * 缓存该接口下实例
     */
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    /**
     * 存放该type接口下的所有类
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private String cachedDefaultName;

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 查看该类是否携带SPI的注解
     *
     * @param clazz 类
     */
    private static <T> boolean withExtensionAnnotation(Class<T> clazz) {
        return clazz.isAnnotationPresent(SPI.class);
    }

    /**
     * 获得ExtensionLoader
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type +
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);

        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    public static <T> T getExtension(Class<T> type, String name) {
        return ExtensionLoader.getExtensionLoader(type).getExtension(name);
    }

    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (StrUtil.hasEmpty(name)) {
            throw new IllegalArgumentException("Extension name is empty");
        }
        if (ExtensionLoaderSupportEnum.DEFAULT.getMsg().equals(name)) {
            return getDefaultExtension();
        }

        Holder<Object> holder = cachedInstances.get(name);

        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }

        Object instance = holder.get();

        if (instance == null) {

            synchronized (holder) {
                
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }

        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name) {

        Class<?> clazz = getExtensionClasses().get(name);

        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }

        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();

        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {


                    classes = loadExtensionClasses();

                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);

        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
                cachedDefaultName = value;
            }
        }

        return loadDirectory();

    }

    private Map<String, Class<?>> loadDirectory() {
        ClazzLoader classLoader = SingletonFactoryUtil.getInstance(ClazzLoader.class);

        Map<String, Class<?>> res = new HashMap<>(8);
        classLoader.loadDirectory(res, type);
        return res;
    }

    public T getDefaultExtension() {
        getExtensionClasses();

        if (null == cachedDefaultName || cachedDefaultName.length() == 0
                || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }
}
