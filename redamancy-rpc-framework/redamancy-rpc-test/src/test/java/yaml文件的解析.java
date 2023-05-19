import fang.redamancy.core.common.asyn.ApplicationContextPro;
import fang.redamancy.core.common.extension.ExtensionLoader;
import fang.redamancy.core.common.model.ExtensionYaml;
import fang.redamancy.core.protocol.serialize.Serializer;
import fang.redamancy.core.remoting.transport.netty.client.NettyRpcClient;
import fang.redamancy.core.remoting.transport.netty.server.NettyRpcServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author redamancy
 * @Date 2022/11/11 09:45
 * @Version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class yaml文件的解析 {

    @Resource
    private NettyRpcClient nettyRpcClient;

    @Resource
    private NettyRpcServer nettyRpcServer;

    @Test
    public void ServerTest() {
//        nettyRpcServer.start();
    }


    @Test
    public void ClientTest() {

        Thread thread = new Thread(() -> {
            Serializer serializer = (Serializer) ApplicationContextPro.getBean("kyro");
            serializer.serialize("test");
        });

        thread.start();
    }

    @Test
    public void RTest() {
        URL url = ExtensionLoader.class.getClassLoader().getResource("META-INF/services/fang.yaml");
        try (BufferedReader inputStream = new BufferedReader(new FileReader(url.getPath()))) {

            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yaml = new Yaml(new Constructor(ExtensionYaml.class), representer);

            ExtensionYaml extensionYaml = yaml.load(inputStream);
            extensionYaml.hashCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ;
    }

    @Test
    public void Rest() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);

        Map<String, Map<String, String>> map = new HashMap<>();
        ExtensionYaml extensionLoader = new ExtensionYaml();
        Map<String, String> s = new HashMap<>();
        s.put("e", "cn.cm.ex");
        map.put("list", s);

        File dumpfile = new File("/Users/redamancy/Desktop/a.yaml");
        try (FileWriter writer = new FileWriter(dumpfile)) {
            yaml.dump(extensionLoader, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
