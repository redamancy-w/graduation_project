package fang.redamancy.core.config;

import fang.redamancy.core.common.annotation.FangService;
import lombok.Data;

/**
 * @Author redamancy
 * @Date 2023/3/7 22:07
 * @Version 1.0
 */

@FangService
@Data
public class TestImpl implements TestInterface {

    @Override
    public void test() {
        System.out.println("test");
    }
}
