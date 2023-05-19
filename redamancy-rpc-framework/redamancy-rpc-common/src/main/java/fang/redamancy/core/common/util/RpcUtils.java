package fang.redamancy.core.common.util;

import cn.hutool.core.util.StrUtil;
import fang.redamancy.core.common.constant.Constants;
import fang.redamancy.core.common.enums.RpcErrorMessageEnum;
import fang.redamancy.core.common.enums.RpcResponseCodeEnum;
import fang.redamancy.core.common.exception.RpcException;
import fang.redamancy.core.common.model.RpcRequest;
import fang.redamancy.core.common.model.RpcResponse;

/**
 * @Author redamancy
 * @Date 2023/4/17 15:50
 * @Version 1.0
 */
public class RpcUtils {


    public static void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {

        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, Constants.INTERFACE_KEY + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, Constants.INTERFACE_KEY + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(!StrUtil.isBlank(rpcResponse.getExceptionMessage()) ? rpcResponse.getExceptionMessage() : RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE.getMessage());
        }
    }
}
