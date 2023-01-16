package fang.redamancy.core.common.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author redamancy
 * @Date 2023/1/9 18:19
 * @Version 1.0
 */
public class SingletonFactoryUtil {

    private static final Map<String, Object> INSTANCES = new ConcurrentHashMap<>();

    private SingletonFactoryUtil() {
    }

    // 静态工厂
    public static <T> T getInstance(Class<T> className) {

        if (className == null) {
            throw new IllegalArgumentException();
        }

        String key = className.toString();

        Object instace = INSTANCES.get(key);

        if (instace == null) {

            synchronized (SingletonFactoryUtil.class) {

                instace = INSTANCES.get(key);
                if (instace == null) {
                    try {
                        instace = className.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                    INSTANCES.put(key, instace);
                }
            }
        }
        return className.cast(instace);
    }
}
