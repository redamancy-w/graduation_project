package fang.redamancy.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author redamancy
 * @Date 2022/11/27 15:35
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum CompressTypeEnum {

    /**
     * 压缩类型
     * gzip
     */
    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

    public static byte getCode(String name) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getName().equals(name)) {
                return c.getCode();
            }
        }
        return GZIP.getCode();
    }
}
