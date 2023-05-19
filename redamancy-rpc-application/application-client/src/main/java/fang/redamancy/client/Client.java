package fang.redamancy.client;

import fang.redamancy.core.config.spring.annotation.EnableFangRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author redamancy
 * @Date 2023/4/28 14:02
 * @Version 1.0
 */
@SpringBootApplication
@EnableFangRpc
public class Client {

    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
    }
}
