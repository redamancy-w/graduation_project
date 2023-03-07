package fang.redamancy.core.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author redamancy
 * @Date 2023/2/13 17:42
 * @Version 1.0
 */
public class NetUtil {

    public static final String LOCALHOST = "127.0.0.1";

    public static String getLocalhost() {
        InetAddress local;
        try {
            local = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return local == null ? LOCALHOST : local.getHostAddress();
    }
}
