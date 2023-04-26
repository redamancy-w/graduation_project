package fang.redamancy.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列化类型
 *
 * @Author redamancy
 * @Date 2022/11/27 15:39
 * @Version 1.0
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {

    /**
     * kyro
     */
    KYRO((byte) 0x01, "kryo"),

    /**
     * hessian
     */
    HESSIAN((byte) 0X02, "hessian");

    private final byte code;
    private final String name;

    public static String getName(byte code) {

        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

    public static byte getCode(String name) {
        for (SerializationTypeEnum serializationTypeEnum : SerializationTypeEnum.values()) {
            if (serializationTypeEnum.getName().equals(name)) {
                return serializationTypeEnum.getCode();
            }
        }

        return 0x0;
    }

}
