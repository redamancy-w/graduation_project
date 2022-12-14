package fang.redamancy.core.remoting.transport.netty.client.bufferpool;

import fang.redamancy.core.common.model.RpcResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author redamancy
 * @Date 2022/11/8 14:57
 * @Version 1.0
 */
public class PendingRequest {

    private static final Map<String, CompletableFuture<RpcResponse<Object>>> PENDING_REQUEST = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        PENDING_REQUEST.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = PENDING_REQUEST.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
