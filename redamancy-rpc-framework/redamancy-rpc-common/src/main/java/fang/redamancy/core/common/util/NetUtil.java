package fang.redamancy.core.common.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * @Author redamancy
 * @Date 2023/2/13 17:42
 * @Version 1.0
 */
public class NetUtil {


    private static final int RND_PORT_START = 30000;

    private static final int RND_PORT_RANGE = 10000;

    private static final Random RANDOM = new Random(System.currentTimeMillis());
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

    public static int getAvailablePort() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(null);
            return ss.getLocalPort();
        } catch (IOException e) {
            return getRandomPort();
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }


    public static int getRandomPort() {
        return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
    }
}
