package fang.redamancy.core.config.support;

import fang.redamancy.core.common.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于解析用户的配置比如一些：
 * 1.注册中心的选择
 * 2.注册中新的配置
 * 3.服务名等
 *
 * @Author redamancy
 * @Date 2023/2/18 16:18
 * @Version 1.0
 */
@Slf4j
public abstract class ServiceConfig implements Serializable {
    private static final long serialVersionUID = 426753353437413570L;
    private static final int  MAX_LENGTH       = 200;


    /**
     * 检查参数是否合法
     *
     * @param property
     * @param value
     * @param pattern
     */
    protected static void checkProperty(String property, String value, Pattern pattern) {
        if (value == null || value.length() == 0) {
            return;
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalStateException("Invalid " + property + "=\"" + value + "\" is longer than " + MAX_LENGTH);
        }

        if (pattern != null) {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                throw new IllegalStateException("Invalid " + property + "=\"" + value + "\" contain illegal charactor, only digit, letter, '-', '_' and '.' is legal.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static void appendParameters(Map<String, String> parameters, Object config) {
        if (config == null) {
            return;
        }
        Method[] methods = config.getClass().getMethods();
        for (Method method : methods) {
            try {
                String name = method.getName();

                boolean ok = (name.startsWith("get") || name.startsWith("is"))
                        && !"getClass".equals(name)
                        && Modifier.isPublic(method.getModifiers())
                        && method.getParameterTypes().length == 0
                        && ConfigUtil.isPrimitive(method.getReturnType());

                if (ok) {

                    int i = name.startsWith("get") ? 3 : 2;
                    String prop = ConfigUtil.camelToSplitName(name.substring(i, i + 1).toLowerCase() + name.substring(i + 1), ".");
                    String key;
                    key = prop;

                    //获得config类的属性
                    Object value = method.invoke(config, new Object[0]);
                    String str = String.valueOf(value).trim();

                    if (value != null && str.length() > 0) {
                        parameters.put(key, str);
                    }

                } else if ("getParameters".equals(name)
                        && Modifier.isPublic(method.getModifiers())
                        && method.getParameterTypes().length == 0
                        && method.getReturnType() == Map.class) {

                    Map<String, String> map = (Map<String, String>) method.invoke(config, new Object[0]);

                    if (map != null && map.size() > 0) {
                        String pre = ("");
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            parameters.put(pre + entry.getKey().replace('-', '.'), entry.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }


    protected void appendAnnotation() {
        return;
    }

    /**
     * 将注解所携带的数据映射到该对象中
     *
     * @param annotationClass
     * @param annotation
     */
    protected void appendAnnotation(Class<?> annotationClass, Object annotation) {
        //获得注解中的方法
        Method[] methods = annotationClass.getMethods();
        for (Method method : methods) {
            // 方法所在类不是Object本身
            if (method.getDeclaringClass() != Object.class
                    // 方法的返回类型不是void
                    && method.getReturnType() != void.class
                    // 方法的参数个数为0
                    && method.getParameterTypes().length == 0
                    // 方法的修饰符为public
                    && Modifier.isPublic(method.getModifiers())
                    // 方法不是静态的
                    && !Modifier.isStatic(method.getModifiers())) {
                try {
                    // 方法名
                    String property = method.getName();
                    if ("interfaceClass".equals(property) || "interfaceName".equals(property)) {
                        property = "interface";
                    }

                    //组装setter方法名 (setGroup, setVersion 等)
                    String setter = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
                    //获得注解上的参数
                    Object value = method.invoke(annotation, new Object[0]);
                    if (value != null && !value.equals(method.getDefaultValue())) {

                        Class<?> parameterType = method.getReturnType();

                        try {
                            //通过反射来调用setter方法
                            Method setterMethod = getClass().getMethod(setter, new Class<?>[]{parameterType});
                            setterMethod.invoke(this, new Object[]{value});
                        } catch (NoSuchMethodException e) {
                            // ignore
                        }
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }


}
