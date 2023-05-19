package fang.redamancy.core.protocol.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author redamancy
 * @Date 2022/12/29 11:14
 * @Version 1.0
 */
@Slf4j
public class KyroSerializerPool {

    /**
     * 保证线程安全使用kryo自带的pool
     */

    private final static Pool<Kryo> KRYO_POOL = new Pool<Kryo>(true, false, 512) {
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

    public static Kryo fetch() {
        try {
            return KRYO_POOL.obtain();
        } catch (Exception ignored) {
            log.error("kryo get filed");
            return null;
        }
    }

    public static void recycle(Kryo kryo) {
        try {
            KRYO_POOL.free(kryo);
        } catch (Exception ignored) {
            log.error("kryo free filed");
        }
    }
}
