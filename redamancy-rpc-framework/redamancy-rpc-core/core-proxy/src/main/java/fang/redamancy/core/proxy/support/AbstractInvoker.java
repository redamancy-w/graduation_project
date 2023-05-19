package fang.redamancy.core.proxy.support;

import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.common.model.RpcConfig;
import fang.redamancy.core.common.model.RpcInvocation;
import fang.redamancy.core.common.model.RpcResponse;
import fang.redamancy.core.proxy.Invoker;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象代理程序执行类
 *
 * @Author redamancy
 * @Date 2023/4/14 14:13
 * @Version 1.0
 */
@Slf4j
@Setter
@Getter
public abstract class AbstractInvoker<T> implements Invoker<T> {

    private final Class<T> type;

    private final RpcConfig rpcConfig;

    private final Map<String, String> attachment;

    private volatile boolean available = true;

    public AbstractInvoker(Class<T> type, RpcConfig rpcConfig) {
        this(type, rpcConfig, (Map<String, String>) null);
    }

    public AbstractInvoker(Class<T> type, RpcConfig rpcConfig, String[] keys) {
        this(type, rpcConfig, convertAttachment(rpcConfig, keys));
    }


    public AbstractInvoker(Class<T> type, RpcConfig rpcConfig, Map<String, String> attachment) {
        if (type == null)
            throw new IllegalArgumentException("service type == null");
        if (rpcConfig == null)
            throw new IllegalArgumentException("service url == null");
        this.type = type;
        this.rpcConfig = rpcConfig;
        this.attachment = attachment == null ? null : Collections.unmodifiableMap(attachment);
    }

    @Override
    public RpcResponse<? extends Object> invoke(RpcInvocation inv) throws RpcException {

        if (attachment != null && attachment.size() > 0) {
            inv.setAttachments(attachment);
        }

        try {

            RpcResponse<?> response = doInvoke(inv);

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void handleException(RpcResponse<?> response) {
    }

    @Override
    public String toString() {
        return getInterface() + " -> " + (getRpcConfig() == null ? "" : getRpcConfig().toString());
    }

    public Class<T> getInterface() {
        return type;
    }

    private static Map<String, String> convertAttachment(RpcConfig rpcConfig, String[] keys) {

        if (keys == null || keys.length == 0) {
            return null;
        }

        Map<String, String> attachment = new HashMap<String, String>();
        for (String key : keys) {
            String value = rpcConfig.getParameter(key);
            if (value != null && value.length() > 0) {
                attachment.put(key, value);
            }
        }
        return attachment;
    }

    protected abstract RpcResponse<?> doInvoke(RpcInvocation invocation) throws Exception;
}
