package fang.redamancy.core.remoting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author redamancy
 * @Date 2022/11/11 09:52
 * @Version 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "fang.redamancy")
public class CoreRemotingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreRemotingApplication.class, args);
    }
}
