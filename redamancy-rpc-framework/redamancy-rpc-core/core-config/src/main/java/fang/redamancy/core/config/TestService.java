package fang.redamancy.core.config;

import fang.redamancy.core.common.annotation.FangReference;
import org.springframework.stereotype.Service;

/**
 * @Author redamancy
 * @Date 2023/4/22 10:57
 * @Version 1.0
 */
@Service
public class TestService {

    @FangReference(version = "1.0.1", group = "def")
    private TestInterface test;

    public String test() {
        return "壮举:成功" + test.test();
    }

}
