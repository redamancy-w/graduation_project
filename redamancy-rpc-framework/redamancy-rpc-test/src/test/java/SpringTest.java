import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @Author redamancy
 * @Date 2023/2/27 13:22
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTest.class)
public class SpringTest {

    @Test
    public void test() throws IOException {
        System.out.println("sss");
        System.in.read();
    }
}
