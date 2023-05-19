package fang.redamancy.core.common.util;

import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.model.RpcConfig;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author redamancy
 * @Date 2023/3/6 14:52
 * @Version 1.0
 */
public class ConfigUtil {

    private static int PID = -1;

    public static int getPid() {
        if (PID < 0) {
            try {
                RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                //runtime = PID@host
                String name = runtime.getName();
                PID = Integer.parseInt(name.substring(0, name.indexOf('@')));
            } catch (Throwable e) {
                PID = 0;
            }
        }
        return PID;
    }

    public static String camelToSplitName(String camelName, String split) {
        if (camelName == null || camelName.length() == 0) {
            return camelName;
        }
        StringBuilder buf = null;
        for (int i = 0; i < camelName.length(); i++) {
            char ch = camelName.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (buf == null) {
                    buf = new StringBuilder();
                    if (i > 0) {
                        buf.append(camelName.substring(0, i));
                    }
                }
                if (i > 0) {
                    buf.append(split);
                }
                buf.append(Character.toLowerCase(ch));
            } else if (buf != null) {
                buf.append(ch);
            }
        }
        return buf == null ? camelName : buf.toString();
    }

    public static RpcConfig parseURL(String address, Map<String, String> defaults) {
        RpcConfig rpcConfig = new RpcConfig();

        if (StringUtils.hasText(address)) {
            String urlInfo;

            //判断是否有://
            if (address.indexOf("://") >= 0) {
                urlInfo = address;
            } else {
                String[] addresses = Constants.COMMA_SPLIT_PATTERN.split(address);
                urlInfo = addresses[0];
            }

            rpcConfig = RpcConfig.valueOf(urlInfo);

        } else {
            String username = defaults == null ? null : defaults.get("username");
            String password = defaults == null ? null : defaults.get("password");
            int port = Integer.parseInt(defaults == null ? "0" : defaults.get("port"));
            String path = defaults == null ? null : defaults.get("path");
            String host = defaults == null ? null : defaults.get("host");
            String protocol = defaults == null ? null : defaults.get("protocol");


            rpcConfig = new RpcConfig(protocol, username, password, host, port, path, null);
        }

        Map<String, String> parameters = defaults == null ? null : new HashMap<String, String>(defaults);

        if (parameters != null) {
            parameters.remove("protocol");
            parameters.remove("username");
            parameters.remove("password");
            parameters.remove("host");
            parameters.remove("port");
            parameters.remove("path");
        }

        rpcConfig.setParameters(parameters);

        return rpcConfig;
    }

    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || type == Character.class
                || type == Boolean.class
                || type == Byte.class
                || type == Short.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class
                || type == Object.class;
    }


}
