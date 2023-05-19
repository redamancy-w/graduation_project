package fang.redamancy.core.common.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Author redamancy
 * @Date 2022/11/21 16:34
 * @Version 1.0
 */
public class RpcConstants {

    /**
     * 魔术字符（标记）
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'f', (byte) 'a', (byte) 'n', (byte) 'g'};

    public static final byte RPC_VERSION = 1;

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final byte TOTAL_LENGTH = 16;

    //远程调用请求
    public static final byte REQUEST_TYPE = 1;

    //表示该请求是远程调用返回
    public static final byte RESPONSE_TYPE = 2;

    /**
     * 心跳检测
     */
    public static final byte HEARTBEAT_REQUEST = 3;


    /**
     * 心跳检测
     */
    public static final byte HEARTBEAT_RESPONSE = 4;

    //魔法值,版本信息,压缩信息等,协议头的长度;
    public static final int HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
}
