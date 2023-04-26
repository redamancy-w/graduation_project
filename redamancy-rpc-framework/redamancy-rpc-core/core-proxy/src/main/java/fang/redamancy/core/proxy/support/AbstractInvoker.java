package fang.redamancy.core.proxy.support;

import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.common.model.RpcInvocation;
import fang.redamancy.core.common.model.RpcResponse;
import fang.redamancy.core.common.net.support.URL;
import fang.redamancy.core.proxy.Invoker;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
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

    private final URL url;

    private final Map<String, String> attachment;

    private volatile boolean available = true;

    public AbstractInvoker(Class<T> type, URL url) {
        this(type, url, (Map<String, String>) null);
    }

    public AbstractInvoker(Class<T> type, URL url, String[] keys) {
        this(type, url, convertAttachment(url, keys));
    }


    public AbstractInvoker(Class<T> type, URL url, Map<String, String> attachment) {
        if (type == null)
            throw new IllegalArgumentException("service type == null");
        if (url == null)
            throw new IllegalArgumentException("service url == null");
        this.type = type;
        this.url = url;
        this.attachment = attachment == null ? null : Collections.unmodifiableMap(attachment);
    }


    @Override
    public RpcResponse<?> invoke(RpcInvocation inv) throws RpcException {

        if (attachment != null && attachment.size() > 0) {
            inv.setAttachments(attachment);
        }
        try {

            return doInvoke(inv);

        } catch (InvocationTargetException e) { // biz exception
            Throwable te = e.getTargetException();
            if (te == null) {
                return new RpcResponse<>(e);
            } else {
                if (te instanceof RpcException) {
                    ((RpcException) te).setCode(RpcException.BIZ_EXCEPTION);
                }
                return new RpcResponse<>(e);
            }
        } catch (RpcException e) {
            if (e.isBiz()) {
                return new RpcResponse<>(e);
            } else {
                throw e;
            }
        } catch (Throwable e) {
            return new RpcResponse<>(e);
        }
    }

    @Override
    public String toString() {
        return getInterface() + " -> " + (getUrl() == null ? "" : getUrl().toString());
    }

    public Class<T> getInterface() {
        return type;
    }

    private static Map<String, String> convertAttachment(URL url, String[] keys) {

        if (keys == null || keys.length == 0) {
            return null;
        }

        Map<String, String> attachment = new HashMap<String, String>();
        for (String key : keys) {
            String value = url.getParameter(key);
            if (value != null && value.length() > 0) {
                attachment.put(key, value);
            }
        }
        return attachment;
    }

    protected abstract RpcResponse<?> doInvoke(RpcInvocation invocation) throws Throwable;
}
