package fang.redamancy.core.remoting.handler;

import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.util.SingletonFactoryUtil;
import fang.redamancy.core.provide.ServiceProvider;
import fang.redamancy.core.provide.support.Impl.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author redamancy
 * @Date 2023/4/17 17:24
 * @Version 1.0
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactoryUtil.getInstance(ServiceProviderImpl.class);
    }

    /**
     * Processing rpcRequest: call the corresponding method, and then return the method
     */
    public Object handle(RpcRequest rpcRequest) {

        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());

        return invokeTargetMethod(rpcRequest, service);

    }

    /**
     * get method execution results
     *
     * @param rpcRequest client request
     * @param service    service object
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.debug("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            if (e instanceof InvocationTargetException) {
                return new RpcException(((InvocationTargetException) e).getTargetException().getMessage());
            }
            return new RpcException(e.getMessage());
        }

        return result;
    }


}
