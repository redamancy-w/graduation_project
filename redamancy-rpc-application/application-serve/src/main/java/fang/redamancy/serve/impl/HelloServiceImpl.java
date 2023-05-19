package fang.redamancy.serve.impl;

import fang.redamancy.api.service.HelloService;
import fang.redamancy.core.common.annotation.FangService;

import java.util.Arrays;

/**
 * @Author redamancy
 * @Date 2023/4/28 13:56
 * @Version 1.0
 */
@FangService(version = "1.0.1", group = "dev")
public class HelloServiceImpl implements HelloService {

    @Override
    public String parameterless() {
        return "无参方法测试";
    }

    @Override
    public String parameterized(String args, String[] argsList) {
        return "有参方法测试参数是:" + args + Arrays.toString(argsList);
    }

    @Override
    public String handleException() {
        throw new RuntimeException("异常处理测试");
    }


    @Override
    public String timeoutTest() {

        try {

            Thread.sleep(20000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return "超时";
    }


    @Override
    public String loadBalancing(String la) {
        return System.getProperty("configurePath");
    }


}
