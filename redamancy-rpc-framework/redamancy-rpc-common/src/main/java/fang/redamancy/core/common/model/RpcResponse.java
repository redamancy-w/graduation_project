package fang.redamancy.core.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

/**
 * @Author redamancy
 * @Date 2022/11/8 15:09
 * @Version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class RpcResponse<T> {

    private String requestId;
}
