package fang.redamancy.core.protocol.serialize.stream;

import fang.redamancy.core.protocol.serialize.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @Author redamancy
 * @Date 2023/5/20 10:49
 * @Version 1.0
 */
public class StreamSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {

        ByteArrayOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            outputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {

        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
