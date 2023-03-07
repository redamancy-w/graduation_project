package fang.redamancy.core.common.constant;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.regex.Pattern;

/**
 * @Author redamancy
 * @Date 2023/3/6 14:37
 * @Version 1.0
 */
public class Constants {

    public static final String  ANYHOST_VALUE          = "0.0.0.0";
    public static final String  TIMESTAMP_KEY          = "timestamp";
    public static final String  PID_KEY                = "pid";
    public static final String  DEFAULT_PROTOCOL       = "nacos";
    public static final Pattern REGISTRY_SPLIT_PATTERN = Pattern
            .compile("\\s*[|;]+\\s*");

    public static final Pattern COMMA_SPLIT_PATTERN = Pattern
            .compile("\\s*[,]+\\s*");
}
