package fang.redamancy.core.common.model;

import fang.redamancy.core.common.constant.nacosattribute.NacosSupport;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 为invoker提供必要的信息
 *
 * @Author redamancy
 * @Date 2023/4/14 10:06
 * @Version 1.0
 */
@Setter
@Getter
public class RpcInvocation {

    private Method method;

    private Object[] args;

    private Map<String, String> attachments;


    public RpcInvocation(Method method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    public String getMethodName() {
        return this.method.getName();
    }

    public String getParameter(String key) {
        String value = attachments.get(key);
        if (value == null || value.length() == 0) {
            value = attachments.get(NacosSupport.DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

}
