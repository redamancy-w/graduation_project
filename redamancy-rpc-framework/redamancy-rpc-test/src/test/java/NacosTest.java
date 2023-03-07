import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.register.api.factory.RegisterFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @Author redamancy
 * @Date 2023/2/4 00:17
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NacosTest.class)
public class NacosTest {

    @Test
    public void BaseTest() throws NacosException, IOException {
        NamingService namingService = NamingFactory.createNamingService("localhost:8848");
        namingService.registerInstance("user", "11.11.11.11", 8888);
        System.in.read();
    }

    @Test
    public void NacosClient() {
        RegisterFactory registerFactory = ExtensionLoader.getExtension(RegisterFactory.class, "nacos");
        System.out.println("sss");
    }

}
