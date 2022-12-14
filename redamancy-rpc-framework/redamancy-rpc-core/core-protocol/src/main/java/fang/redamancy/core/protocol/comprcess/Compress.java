package fang.redamancy.core.protocol.comprcess;

/**
 * @Author redamancy
 * @Date 2022/11/27 16:06
 * @Version 1.0
 */
public interface Compress {

    /**
     * 压缩
     * @param bytes
     * @return
     */ byte[] compress(byte[] bytes);

    /**
     * 解压
     * @param bytes
     * @return
     */
    byte[] decompress(byte[] bytes);
}
