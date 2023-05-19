package fang.redamancy.core.common.model;

import fang.redamancy.core.common.enums.RpcResponseCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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
     * exception Message;
     */
    private String exceptionMessage;

    private T data;


    public T getData() {
        return data;
    }


    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (!Objects.isNull(data)) {
            response.setData(data);
        }
        return response;

    }


    public static <T> RpcResponse<T> fail(Throwable exception, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.FAIL.getCode());
        response.setMessage(RpcResponseCodeEnum.FAIL.getMessage());
        response.setRequestId(requestId);
        if (!Objects.isNull(exception)) {
            response.setExceptionMessage(exception.getMessage());
        }

        return response;
    }

}
