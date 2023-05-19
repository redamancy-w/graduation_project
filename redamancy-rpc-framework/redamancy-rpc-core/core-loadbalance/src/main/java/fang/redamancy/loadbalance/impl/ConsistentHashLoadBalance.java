package fang.redamancy.loadbalance.impl;

import cn.hutool.crypto.digest.DigestUtil;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.loadbalance.AbstractLoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author redamancy
 * @Date 2023/4/18 17:44
 * @Version 1.0
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {


    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();


    @Override
    protected RpcConfig doSelect(List<RpcConfig> serviceAddresses, RpcRequest rpcRequest) {

        String identityHashCode = DigestUtil.sha256Hex(serviceAddresses.toString());

        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);

        if (selector == null || !selector.identityHashCode.equals(identityHashCode)) {

            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));

            selector = selectors.get(rpcServiceName);
        }

        String parameters = rpcRequest.getParameters() == null ? "" : Arrays.toString(rpcRequest.getParameters());

        return selector.select(rpcServiceName + parameters);

    }

    static class ConsistentHashSelector {
        private final TreeMap<Long, RpcConfig> virtualInvokers;

        private final String identityHashCode;

        ConsistentHashSelector(List<RpcConfig> invokers, int replicaNumber, String identityHashCode) {

            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (RpcConfig invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker.getAddress().getHostString() + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;

        }

        public RpcConfig select(String rpcServiceKey) {
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest, 0));
        }

        public RpcConfig selectForKey(long hashCode) {
            Map.Entry<Long, RpcConfig> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();

            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }

}
