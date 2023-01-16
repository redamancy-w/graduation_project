package fang.redamancy.core.remoting;

import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.protocol.serialize.Serializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author redamancy
 * @Date 2023/1/13 15:56
 * @Version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SPI测试 {

    @Test
    public void test() {
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("kryo");
        serializer.serialize("test");
    }

}
