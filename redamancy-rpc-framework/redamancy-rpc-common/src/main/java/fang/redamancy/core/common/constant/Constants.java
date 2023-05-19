package fang.redamancy.core.common.constant;

import java.util.regex.Pattern;

/**
 * @Author redamancy
 * @Date 2023/3/6 14:37
 * @Version 1.0
 */
public class Constants {

    public static final String ANYHOST_VALUE = "0.0.0.0";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String PID_KEY = "pid";
    public static final String DEFAULT_PROTOCOL = "nacos";
    public static final Pattern REGISTRY_SPLIT_PATTERN = Pattern
            .compile("\\s*[|;]+\\s*");

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern
            .compile("\\s*[,]+\\s*");
    public static final String INTERFACE_KEY = "interfaceName";
    public static final String GROUP_KEY = "group";
    public static final String GROUP_DEFAULT = "DEFAULT_GROUP";

    public static final String TOKEN_KEY = "token";
    public static final String TIMEOUT_KEY = "timeout";
    public static final String VERSION_KEY = "version";

    public static final String VERSION_DEFAULT = "0.0.0";
    public static final int DEFAULT_TIMEOUT = 5000;
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    public static final String TEMP_URL = "temp://localhost:0";
    public static final String PROTOCOL_KEY = "protocol";
    public static final String TRANSPORT = "transport";
    public static final String TRANSPORT_DEFAULT = "netty";

    public static final String SERIALIZE = "serialize";

    public static final String SERIALIZE_DEFAULT = "kryo";

    public static final String COMPRESS = "compress";

    public static final String COMPRESS_DEFAULT = "gizp";
    public static final String LOAD_BALANCE = "load.balance";
    public static final String LOAD_BALANCE_DEFAULT = "random";
    public static final String IS_SERVER = "is.server";
    public static final String BIND_PORT = "bind.port";
    public static final int BIND_PORT_DEFAULT = 20049;
    public static final String SHORT_CONNECTION = "short.connection";
    public static final String SHORT_CONNECTION_DEFAULT = "false";
}
