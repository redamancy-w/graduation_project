package fang.redamancy.loadbalance.impl;

import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.loadbalance.AbstractLoadBalance;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/**
 * @Author redamancy
 * @Date 2023/4/18 17:40
 * @Version 1.0
 */
public class RandomLoadBalance extends AbstractLoadBalance {


    private Random random;

    @Override
    protected URL doSelect(List<URL> serviceAddresses, RpcRequest rpcRequest) {
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));

    }
}
