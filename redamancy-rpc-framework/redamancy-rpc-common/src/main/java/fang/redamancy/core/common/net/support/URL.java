package fang.redamancy.core.common.net.support;

import fang.redamancy.core.common.constant.nacosattribute.NacosSupport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
     * 服务名
     */
    private String interfaceName;


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
    private String  username;
    /**
     * 注册中心客户端密码
     */
    private String  password;
    /**
     * 注册中心ip
     */
    private String  host;
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
}
