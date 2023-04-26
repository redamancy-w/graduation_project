package fang.redamancy.core.common.model;

import fang.redamancy.core.common.enums.RpcResponseCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author redamancy
 * @Date 2022/11/8 15:09
 * @Version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> {

    private String requestId;
    /**
     * response code
     */
    private Integer code;
    /**
     * response message
     */
    private String message;
    /**
     * 抛回的异常
     */
    private Throwable exception;

    public RpcResponse(Throwable exception) {
        this.exception = exception;
    }

    public static RpcResponse<Object> success(Object result, String requestId) {
        return null;


    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }

}
