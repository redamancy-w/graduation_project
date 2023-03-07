import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.protocol.serialize.Serializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author redamancy
 * @Date 2023/2/5 23:13
 * @Version 1.0
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SPITest.class)
public class SPITest {

    @Test
    public void test() {
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("kryo");
        serializer = ExtensionLoader.getExtension(Serializer.class, "kryo");
        serializer.serialize("test");
    }
}
