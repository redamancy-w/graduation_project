package fang.redamancy.client.controller;

import fang.redamancy.api.service.HelloService;
import fang.redamancy.core.common.annotation.FangReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author redamancy
 * @Date 2023/4/28 14:03
 * @Version 1.0
 */

@RestController
public class HelloController {


    @FangReference(version = "1.0.1", group = "dev")
    private HelloService helloService;

    @FangReference(version = "1.0.2", group = "dev", timeout = "30000")
    private HelloService helloServiceC;

    @GetMapping("PLtest")
    public String test() {
        return helloService.parameterless();
    }

    @GetMapping("PTest")
    public String test(@RequestParam("test") String test) {
        return helloService.parameterized(test, new String[]{"你", "好"});
    }

    @GetMapping("Exception")
    public String eTest() {
        return helloService.handleException();
    }

    @GetMapping("Loadbalanc")
    public String loadTest(@RequestParam("la") String la) {

        return helloService.loadBalancing(la);
    }

    @GetMapping("timeout")
    public String timeOut() {
        return helloService.timeoutTest();
    }


    @GetMapping("version")
    public String version() {
        return helloServiceC.parameterless();
    }

    @GetMapping("timeoutS")
    public String timeoutS() {
        return helloServiceC.timeoutTest();
    }
}
