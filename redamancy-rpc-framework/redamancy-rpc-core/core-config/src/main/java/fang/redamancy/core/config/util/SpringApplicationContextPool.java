package fang.redamancy.core.config.util;

import cn.hutool.core.collection.ConcurrentHashSet;
import org.springframework.context.ApplicationContext;

import java.util.Set;

/**
 * @Author redamancy
 * @Date 2023/3/4 19:58
 * @Version 1.0
 */
public class SpringApplicationContextPool {
    private static final Set<ApplicationContext> contexts = new ConcurrentHashSet<ApplicationContext>();

    public static void addApplicationContext(ApplicationContext context) {
        contexts.add(context);
    }

    public static void removeApplicationContext(ApplicationContext context) {
        contexts.remove(context);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<T> type, String name) {
        for (ApplicationContext context : contexts) {
            if (context.containsBean(name)) {
                Object bean = context.getBean(name);
                if (type.isInstance(bean)) {
                    return (T) bean;
                }
            }
        }
        return null;
    }

}
