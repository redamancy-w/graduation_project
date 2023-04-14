package fang.redamancy.core.config;

import fang.redamancy.core.common.util.NetUtil;
import fang.redamancy.core.config.spring.annotation.EnableFangRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author redamancy
 * @Date 2023/2/27 13:33
 * @Version 1.0
 */
@SpringBootApplication
@EnableFangRpc
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println(NetUtil.getAvailablePort());
    }
}

