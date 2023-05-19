package fang.redamancy.core.protocol.regulation;

import fang.redamancy.core.common.constant.RpcConstants;
import fang.redamancy.core.common.enums.CompressTypeEnum;
import fang.redamancy.core.common.enums.SerializationTypeEnum;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.model.RpcMessage;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.model.RpcResponse;
import fang.redamancy.core.protocol.compress.Compress;
import fang.redamancy.core.protocol.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * LengthFieldBasedFrameDecoder类是Netty提供的用来解析带长度字段数据包的类，继承自ByteToMessageDecoder类。
 *
 * @Author redamancy
 * @Date 2022/11/17 16:54
 * @Version 1.0
 * *  0  1  2  3 4   5  6  7  8    9             10      11         12  13 14   15
 * * ┌───────────┬───┬─────────────┬──────────────┬───────┬─────────┬────────────┐
 * * │magic code │ -V│  full length│  messageType │ codec │ compress│  requestId │
 * * │           │   │             │              │       │         │            │
 * * ├───────────┴───┴─────────────┴──────────────┴───────┴─────────┴────────────┤
 * * │                                                                           │
 * * │                                                                           │
 * * │                                                                           │
 * * │                    body ......                                            │
 * * │                                                                           │
 * * │                                                                           │
 * * │                                                                           │
 * * └───────────────────────────────────────────────────────────────────────────┘
 * <p>
 * * 4B  magic code（魔法数) 1B -V (应用版本)        4B full length（消息长度）
 * 1B messageType（消息类型） 1B codec（序列化类型）   1B compress（压缩类型）
 * 4B  requestId（请求的Id）
 * body（object类型数据）
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */

@Slf4j
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                      int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    /**
     * lengthFieldOffset: magic code + version 的长度是5
     * lengthFieldLength: full length 是4，所以值是4
     * lengthAdjustment: fulllength 是4，macgic code是4，full length 是数据全长，所以，还需要读 full lenfth 的值 - （full length + macgic code），所以是-9
     * initialBytesToStrip: 因为是手动检测的，所以不需要剥离
     * <p>
     * 1.从消息开头偏移lengthFieldOffset长度, 到达A位置
     * <p>
     * 2.再从A位置读取lengthFieldLength长度, 到达B位置, 内容是d
     * <p>
     * 3.再从B位置读取(d+lengthAdjustment)长度, 达到D位置
     * <p>
     * 4.从消息开头跳过initialBytesToStrip长度到达C位置
     * <p>
     * 5.将C位置-D位置之间的内容传送给接下来的处理器进行后续处理
     */
    public RpcDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }

        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        // 字节流是不重置的，所以要按照规定顺序读取
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        // 创建RpcMessage
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();

        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .compress(compressType)
                .messageType(messageType).build();

        if (messageType == RpcConstants.HEARTBEAT_REQUEST) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }

        if (messageType == RpcConstants.HEARTBEAT_RESPONSE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }

        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;

        if (bodyLength > 0) {
            handleBody(in, bodyLength, rpcMessage);
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        //比较双方的框架版本
        byte version = in.readByte();
        if (version != RpcConstants.RPC_VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    /**
     * 处理body体
     *
     * @param in         入字节流
     * @param bodyLength body体的长度
     * @param rpcMessage 前置的rpcMessage信息
     */
    private void handleBody(ByteBuf in, Integer bodyLength, RpcMessage rpcMessage) {
        byte[] bs = new byte[bodyLength];
        in.readBytes(bs);

        //gzip解压对象
        String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
        log.info("compress name : [{}]", compressName);
        Compress compress = ExtensionLoader.getExtension(Compress.class, compressName);
        bs = compress.decompress(bs);

        //反序列化对象
        String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
        log.debug("codec name: [{}] ", codecName);
        Serializer serializer = ExtensionLoader.getExtension(Serializer.class, codecName);

        if (rpcMessage.getMessageType() == RpcConstants.REQUEST_TYPE) {

            RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
            rpcMessage.setData(tmpValue);

        } else {
            RpcResponse<?> tmpValue = serializer.deserialize(bs, RpcResponse.class);
            rpcMessage.setData(tmpValue);
        }

    }

    /**
     * 检测请求体的魔法数，是否与设定的相同，证明其是一个自定义的协议体
     *
     * @param in 字节流
     */
    private void checkMagicNumber(ByteBuf in) {
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}
