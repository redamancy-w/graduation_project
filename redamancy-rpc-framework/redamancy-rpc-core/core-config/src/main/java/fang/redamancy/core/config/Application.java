package fang.redamancy.core.config;

import fang.redamancy.core.config.spring.annotation.EnableFangRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author redamancy
 * @Date 2023/2/27 13:33
 * @Version 1.0
 */
@SpringBootApplication
@EnableFangRpc
@RestController
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Resource
    private TestService testService;

    @GetMapping("test")
    public String test() {
        return testService.test();

    }

}

