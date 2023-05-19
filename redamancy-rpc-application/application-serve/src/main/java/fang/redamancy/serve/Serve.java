package fang.redamancy.serve;

import fang.redamancy.core.config.spring.annotation.EnableFangRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author redamancy
 * @Date 2023/4/28 13:55
 * @Version 1.0
 */
@SpringBootApplication
@EnableFangRpc
public class Serve {


    public static void main(String[] args) {
        SpringApplication.run(Serve.class, args);
    }

}
