package fang.redamancy.core.protocol.regulation;

import fang.redamancy.core.common.asyn.ApplicationContextPro;
import fang.redamancy.core.common.constant.RpcConstants;
import fang.redamancy.core.common.enums.CompressTypeEnum;
import fang.redamancy.core.common.enums.SerializationTypeEnum;
import fang.redamancy.core.common.model.RpcMessage;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.model.RpcResponse;
import fang.redamancy.core.protocol.comprcess.Compress;
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
 * *   0     1     2     3     4        5     6     7     8         9        10      11     12  13  14   15
 * *   +-----+-----+-----+-----+----+----+---+-----+-----+-----------+-------+-------+--------------------------|
 * *   |   magic   code        |     full length        | messageType| codec|compress|    RequestId             |
 * *   +-----------------------+------------------------+------------+-----------+-----------+-----------------+
 * *   |                                                                                                       |
 * *   |                                         body                                                          |
 * *   |                                                                                                       |
 * *   |                                        ... ...                                                        |
 * *   +-------------------------------------------------------------------------------------------------------+
 * * 4B  magic code（魔法数) 4B full length（消息长度）    1B messageType（消息类型）
 * * 1B codec（序列化类型）   1B compress（压缩类型）       4B  requestId（请求的Id）
 * * body（object类型数据）
 *
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                      int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(in);
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

    /**
     * lengthFieldOffset: magic code 的长度是4
     * lengthFieldLength: full length 是4，所以值是4
     * lengthAdjustment: fulllength 是4，macgic code是4，full length 是数据全长，所以，还需要读 full lenfth 的值 - （full length + macgic code），所以是-8
     * initialBytesToStrip: 因为是手动检测的，所以不需要剥离
     */
    public RpcDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH, 4, 4, -8, 0);
    }


    private Object decodeFrame(ByteBuf in) {
        // 字节流是不重置的，所以要按照规定顺序读取
        checkMagicNumber(in);
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
            handleBody(in,bodyLength,rpcMessage);
        }
        return rpcMessage;
    }

    /**
     * 处理body体
     *
     * @param in         入字节流
     * @param bodyLength body体的长度
     * @param rpcMessage 前置的rpcMessage信息
     */
    private void handleBody(ByteBuf in, Integer bodyLength, RpcMessage rpcMessage){
        byte[] bs = new byte[bodyLength];
        in.readBytes(bs);

        //gzip解压对象
        String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
        log.info("compress name : [{}]",compressName);
        Compress compress = ApplicationContextPro.getBean(compressName, Compress.class);
        bs = compress.decompress(bs);

        //反序列化对象
        String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
        log.info("codec name: [{}] ", codecName);
        Serializer serializer = ApplicationContextPro.getBean(codecName, Serializer.class);

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
