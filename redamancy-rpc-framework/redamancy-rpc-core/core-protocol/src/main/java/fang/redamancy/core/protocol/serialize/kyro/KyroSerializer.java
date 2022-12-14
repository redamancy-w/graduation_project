package fang.redamancy.core.protocol.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import fang.redamancy.core.common.exception.SerializeException;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.model.RpcResponse;
import fang.redamancy.core.protocol.serialize.Serializer;
import org.checkerframework.checker.units.qual.K;
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
     * 保证线程安全使用kryo自带的pool
     */
    private final static Pool<Kryo> kryoPool = new Pool<Kryo>(true,false,8) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();

            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);
            kryo.register(RpcResponse.class);
            kryo.register(RpcRequest.class);

            return kryo;
        }
    };

    public static void main(String[] args) {

    }

    /**
     * Kryo并不是线程安全的
     * 用ThreadLocal来存储实例副本
     */
//    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
//        Kryo kryo = new Kryo();
//        kryo.register(RpcResponse.class);
//        kryo.register(RpcRequest.class);
//        return kryo;
//    });

    @Override
    public byte[] serialize(Object obj) {
//        Kryo kryo = null;
        Kryo kryo = kryoPool.obtain();

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }finally {
            kryoPool.free(kryo);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            kryo = kryoPool.obtain();
            // byte->Object:从byte数组中反序列化出对对象
            Object o = kryo.readObject(input, clazz);
            kryoPool.free(kryo);
            return clazz.cast(o);
        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }
    }
}
