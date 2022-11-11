package fang.redamancy.core.remoting.transport;

/**
 * @Author redamancy
 * @Date 2022/11/7 15:49
 * @Version 1.0
 */

/**
 * rpc 客户端接口
 */
public interface RpcRequestTransport {


    /**
     * 客户端请求函数
     * @param request 客户端请求参数
     * @return
     */
    Object sendRpcRequest(Object request);
}
