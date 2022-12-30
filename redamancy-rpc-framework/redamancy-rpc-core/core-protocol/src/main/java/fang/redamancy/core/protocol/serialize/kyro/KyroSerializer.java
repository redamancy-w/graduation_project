package fang.redamancy.core.protocol.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fang.redamancy.core.common.exception.SerializeException;
import fang.redamancy.core.protocol.serialize.Serializer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @Author redamancy
 * @Date 2022/11/11 10:47
 * @Version 1.0
 */
@Component(value = "kyro")
public class KyroSerializer implements Serializer {


    /**
     * Kryo并不是线程安全的
     * 用ThreadLocal来存储实例副本
     * private final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
     * Kryo kryo = new Kryo();
     * kryo.setRegistrationRequired(false);
     * kryo.setReferences(false);
     * kryo.register(RpcResponse.class);
     * kryo.register(RpcRequest.class);
     * return kryo;
     * });
     */


    @Override
    public byte[] serialize(Object obj) {


        Kryo kryo = KyroSerializerPool.fetch();

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            // Object->byte:将对象序列化为byte数组
            assert kryo != null;
            kryo.writeObject(output, obj);
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        } finally {
            KyroSerializerPool.recycle(kryo);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = KyroSerializerPool.fetch();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {

            // byte->Object:从byte数组中反序列化出对对象
            assert kryo != null;
            Object o = kryo.readObject(input, clazz);

            return clazz.cast(o);

        } catch (Exception e) {

            throw new SerializeException("Deserialization failed");

        } finally {
            KyroSerializerPool.recycle(kryo);
        }
    }
}
