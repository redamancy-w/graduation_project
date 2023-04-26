package fang.redamancy.core.common.net.support;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.constant.nacosattribute.NacosSupport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于请求注册中心中的节点
 *
 * @Author redamancy
 * @Date 2023/1/17 20:20
 * @Version 1.0
 */
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public final class URL implements Serializable {


    /**
     * 服务id
     */
    private String rpcServiceId;


    /**
     * 注册中心
     * nacos
     * redis
     * 等
     */
    private String protocol;

    /**
     * 注册中心客户端user
     */
    private String username;
    /**
     * 注册中心客户端密码
     */
    private String password;
    /**
     * 注册中心ip
     */
    private String host;
    /**
     * 注册中心端口
     */
    private Integer port;

    /**
     * path
     */
    private String path;

    /**
     * 参数
     */
    private Map<String, String> parameters;

    private volatile transient Map<String, Number> numbers;

    private Map<String, Number> getNumbers() {
        if (numbers == null) {
            numbers = new ConcurrentHashMap<String, Number>();
        }
        return numbers;
    }


    public String getRpcServiceKey(String serviceName) {
        StringBuilder sb = new StringBuilder()
                .append(getParameter(Constants.GROUP_KEY, Constants.GROUP_DEFAULT))
                .append(":")
                .append(StringUtils.hasText(serviceName) ? serviceName : getParameter(Constants.INTERFACE_KEY))
                .append(":")
                .append(getParameter(Constants.VERSION_KEY, Constants.VERSION_DEFAULT));

        return String.valueOf(sb);
    }

    public int getMethodParameter(String method, String key, int defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(methodKey, i);
        return i;
    }

    public String getMethodParameter(String method, String key) {
        String value = parameters.get(method + "." + key);
        if (value == null || value.length() == 0) {
            return getParameter(key);
        }
        return value;
    }


    public String getParameter(String key) {
        String value = parameters.get(key);
        if (value == null || value.length() == 0) {
            value = parameters.get(NacosSupport.DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    public int getParameter(String key, int defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        return i;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }


    public static URL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }

        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;

        int i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0) {
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            }
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) {
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                }
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }


        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }


        if (url.length() > 0) {
            host = url;
        }


        return new URL(protocol, username, password, host, port, path, null);
    }

    public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public void putValue(String key, String value) {
        this.parameters.put(key, value);
    }

    public URL(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {

        boolean ok = (username == null || username.length() == 0)
                && password != null && password.length() > 0;

        if (ok) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        // trim the beginning "/"
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }

        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public InetSocketAddress getAddress() {

        return InetSocketAddress.createUnresolved(getHost(), getPort());
    }

    public String getUrl() {
        StringBuilder buf = new StringBuilder();
        buf.append(getHost()).append(":").append(getPort());

        if (StringUtils.hasText(getUsername())) {
            buf.append("@").append(username);
            if (StringUtils.hasText(getPassword())) {
                buf.append(":")
                        .append(password);
            }
        }

        return String.valueOf(buf);
    }
}
