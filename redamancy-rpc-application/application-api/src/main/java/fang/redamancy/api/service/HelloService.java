package fang.redamancy.api.service;

/**
 * @Author redamancy
 * @Date 2023/4/28 13:48
 * @Version 1.0
 */
public interface HelloService {


    String parameterless();

    String parameterized(String args, String[] argsList);

    String handleException();

    String timeoutTest();

    String loadBalancing(String la);

}
