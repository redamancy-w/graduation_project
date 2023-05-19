package fang.redamancy.core.config;

import cn.hutool.core.thread.ThreadUtil;
import fang.redamancy.core.common.annotation.FangService;

import java.util.concurrent.TimeUnit;

/**
 * @Author redamancy
 * @Date 2023/3/7 22:07
 * @Version 1.0
 */

@FangService(version = "1.0.1", group = "def")
public class TestImpl implements TestInterface {

    @Override
    public String test() {

//        throw new RpcException("abcabc");

        ThreadUtil.sleep(12, TimeUnit.SECONDS);
        return "test";
    }
}
