package fang.redamancy.core.config;

import fang.redamancy.core.common.annotation.FangService;

/**
 * @Author redamancy
 * @Date 2023/3/7 22:07
 * @Version 1.0
 */

@FangService(version = "1.0.1", group = "def")
public class TestImpl implements TestInterface {

    @Override
    public String test() {
        System.out.println("test");
        return "test";
    }
}
