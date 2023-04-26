package fang.redamancy.core.common.model;

import fang.redamancy.core.common.constant.Constants;
import lombok.*;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * @Author redamancy
 * @Date 2022/11/11 10:54
 * @Version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    public String getRequestId() {
        return requestId;
    }

    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName() {
        StringBuilder sb = new StringBuilder()
                .append(StringUtils.hasText(group) ? group : Constants.GROUP_DEFAULT)
                .append(":")
                .append(interfaceName)
                .append(":")
                .append(StringUtils.hasText(version) ? version : Constants.VERSION_DEFAULT);
        return String.valueOf(sb);
    }
}
