package fang.redamancy.core.remoting;

import fang.redamancy.core.common.asyn.ApplicationContextPro;
import fang.redamancy.core.protocol.serialize.Serializer;
import fang.redamancy.core.remoting.transport.netty.server.NettyRpcServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author redamancy
 * @Date 2023/1/10 12:25
 * @Version 1.0
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class 整体流程测试 {

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
    public void e() {
        Map<String, String> map = new HashMap<>();
        System.out.println(map.get("list"));
    }
}
