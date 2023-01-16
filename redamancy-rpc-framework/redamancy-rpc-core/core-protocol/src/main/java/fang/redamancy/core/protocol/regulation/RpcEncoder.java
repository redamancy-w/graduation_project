package fang.redamancy.core.protocol.regulation;

import fang.redamancy.core.common.constant.RpcConstants;
import fang.redamancy.core.common.enums.CompressTypeEnum;
import fang.redamancy.core.common.enums.SerializationTypeEnum;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.model.RpcMessage;
import fang.redamancy.core.protocol.compress.Compress;
import fang.redamancy.core.protocol.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * netty编码器
 *
 * @Author redamancy
 * @Date 2022/12/4 17:15
 * @Version 1.0
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {

    /**
     * 请求id
     */
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out) {
        try {
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            ByteBuf mkOut = out.markWriterIndex();
            //预留4的长度后续填入 full length
            out.writerIndex(out.writerIndex() + 4);

            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());

            buildBody(rpcMessage, out, mkOut);

        } catch (Exception e) {
            log.error("Encode error!", e);
        }
    }

    private void buildBody(RpcMessage rpcMessage, ByteBuf out, ByteBuf mkOut) {

        byte[] bodyBytes = null;
        byte messageType = rpcMessage.getMessageType();

        int fullLength = RpcConstants.HEAD_LENGTH;
        if (messageType != RpcConstants.HEARTBEAT_REQUEST
                && messageType != RpcConstants.HEARTBEAT_RESPONSE) {

            //降内容序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);

            Serializer serializer = ExtensionLoader.getExtension(Serializer.class, codecName);

            bodyBytes = serializer.serialize(rpcMessage.getData());

            //压缩内容
            String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
            log.info("compress name: [{}]", compressName);
            Compress compress = ExtensionLoader.getExtension(Compress.class, compressName);

            bodyBytes = compress.compress(bodyBytes);
            fullLength += bodyBytes.length;
        }

        if (bodyBytes != null) {
            out.writeBytes(bodyBytes);
        }
        mkOut.writeInt(fullLength);
    }

}
