package fang.redamancy.core.remoting.model;

import lombok.Getter;

/**
 * @Author redamancy
 * @Date 2022/11/8 15:09
 * @Version 1.0
 */
@Getter
public class RpcResponse<T> {

    private String requestId;
}
