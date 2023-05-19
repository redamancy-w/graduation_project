package fang.redamancy.core.common.model;

import lombok.*;

/**
 * 用于rpc协议传输的数据载体
 *
 * @Author redamancy
 * @Date 2022/11/24 21:15
 * @Version 1.0
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RpcMessage {
    /**
     * 消息类型
     */
    private byte messageType;
    /**
     * 序列化方式
     */
    private byte codec;
    /**
     * 压缩方式
     */
    private byte compress;
    /**
     * request id
     */
    private int requestId;

    /**
     * request body
     */
    private Object data;
}
